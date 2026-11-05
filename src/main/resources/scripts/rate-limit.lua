local countKey = KEYS[1]
local blockKey = KEYS[2]
local max = tonumber(ARGV[1])
local windowSec = tonumber(ARGV[2])
local blockSec = tonumber(ARGV[3])

local current = redis.call('INCR', countKey)
if current == 1 then
    redis.call('EXPIRE', countKey, windowSec)
end
if current >= max then
    redis.call('DEL', countKey)
    redis.call('SETEX', blockKey, blockSec, '1')
end
return current
