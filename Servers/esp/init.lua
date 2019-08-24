node.setcpufreq(node.CPU160MHZ)

local columns = 42
local rows = 24
local dataPin = 6;
local clockPin = 7;
local oneWirePin = 5;
local oneWires = {}
local temps = {}
local overHeat = false;

function bxor(a,b)
   local r = 0
   for i = 0, 31 do
      if ( a % 2 + b % 2 == 1 ) then
         r = r + 2^i
      end
      a = a / 2
      b = b / 2
   end
   return r
end


function checkTemps()
    overHeat = false;
    for id,temp in pairs(temps) do
        if temp > 5000 then
            overHeat = true;
        end
    end
end

local nextSensor = 1;
local lastSensor = 0;
local requestNotSend = false;
function temploop()
    
    if(requestNotSend)then
        ow.reset(oneWirePin)
        ow.select(oneWirePin, oneWires[nextSensor])
        ow.write(oneWirePin, 0x44, 1)
        requestNotSend = false;
    else
        local present = ow.reset(oneWirePin)
        if not present == 1 then
            print("P="..present)  
        end
        ow.select(oneWirePin, oneWires[nextSensor])
        ow.write(oneWirePin,0xBE,1)
        
        local data = nil
        data = string.char(ow.read(oneWirePin))
        for i = 1, 8 do
            data = data .. string.char(ow.read(oneWirePin))
        end
        local crc = ow.crc8(string.sub(data,1,8))
        if crc == data:byte(9) then
            t = (data:byte(1) + data:byte(2) * 256) * 62
            if(t > 10000)then
                t = t/10;
            end
            print("Last temp: " .. nextSensor .. ":" .. t)
            temps[nextSensor] = t;
            checkTemps();
        end
        requestNotSend = true 
        nextSensor = nextSensor +1;
    end


    if(nextSensor > 4)then
        nextSensor = 1;
    end
end

local lighttick = false
function lightloop()
    lighttick = not lighttick;
    a = 25
    b = 25
    g = 25
    r = 0
    if(lighttick)then
        r = 25;
        g = 0;
    end

    if(overHeat)then
        a = 5
        b = 0;
        g = 1;
        r = 1;
    end
    leds_abgr = string.rep(string.char(a, b, g, r),columns*rows)
    apa102.write(dataPin, clockPin, leds_abgr)
end

function initOneWire()

    local oneWireAddress = {}
    ow.setup(oneWirePin)
    ow.reset_search(oneWirePin)
    local count = 0
    while(count < 10) do
        count = count + 1
        local addr = ow.search(oneWirePin)
        if(addr and not oneWireAddress[addr])then
            oneWireAddress[addr]=true;
            crc = ow.crc8(string.sub(addr,1,7))
            if crc == addr:byte(8) then
                if (addr:byte(1) == 0x10) or (addr:byte(1) == 0x28) then
                    print("Device is a DS18S20 family device.")
                    print(addr:byte(1,8))
                end
             end
        end
    end
    for addr,id in pairs(oneWireAddress) do
        table.insert(oneWires,addr)
    end

    lastSensor = table.getn(oneWires);
    print("Finishd scanning, found " .. table.getn(oneWires));
end

function savelightloop()
    local success,msg = pcall(lightloop)
    if not success then
        print(msg)
    end
end


function savetemploop()
    local success,msg = pcall(temploop)
    if not success then
        print(msg)
    end
end

function initialize()
    initOneWire()
    tmr.alarm(0, 130, tmr.ALARM_AUTO, savelightloop)
    tmr.alarm(1, 500, tmr.ALARM_AUTO, savetemploop)
end



local success,msg = pcall(initialize)
if not success then
    print(msg)
end