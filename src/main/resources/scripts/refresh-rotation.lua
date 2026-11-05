local tokenKey = KEYS[1]
local familyKey = KEYS[2]
local userKey = KEYS[3]
local newTokenKey = KEYS[4]

local graceMs = tonumber(ARGV[1])
local tokenTtlMs = tonumber(ARGV[2])
local newHash = ARGV[3]
local newTokenJson = ARGV[4]

local data = redis.call('GET', tokenKey)
if data == false then
  return 0
end

local status = nil
local okStatus, parsed = pcall(cjson.decode, data)
if okStatus and parsed then
  status = parsed['status']
end

if status == nil then
  return 0
end

if status == 'ROTACIONADO' then
  local hashes = redis.call('SMEMBERS', familyKey)
  if hashes and #hashes > 0 then
    for i=1, #hashes do
      local h = hashes[i]
      redis.call('DEL', 'refresh:token:' .. h)
    end
  end
  redis.call('DEL', familyKey)
  if userKey and userKey ~= '' then
    local familiaId = nil
    if okStatus and parsed then
      familiaId = parsed['familiaId']
    end
    if familiaId then
      redis.call('SREM', userKey, familiaId)
    end
  end
  return 2
end

if status == 'ATIVO' then
  local rotado = data
  if okStatus and parsed then
    parsed['status'] = 'ROTACIONADO'
    local rotadoOk, rotadoJson = pcall(cjson.encode, parsed)
    if rotadoOk then
      rotado = rotadoJson
    else
      rotado = data
    end
  else
    rotado = data
  end
  if graceMs and graceMs > 0 then
    redis.call('PSETEX', tokenKey, graceMs, rotado)
  else
    redis.call('PSETEX', tokenKey, 1, rotado)
  end
  if newTokenKey and newTokenKey ~= '' and newHash and newHash ~= '' and newTokenJson and newTokenJson ~= '' then
    redis.call('PSETEX', newTokenKey, tokenTtlMs, newTokenJson)
    redis.call('SADD', familyKey, newHash)
    redis.call('PEXPIRE', familyKey, tokenTtlMs)
    if userKey and userKey ~= '' and okStatus and parsed and parsed['familiaId'] then
      redis.call('SADD', userKey, parsed['familiaId'])
    end
  end
  return 1
end

return 0
