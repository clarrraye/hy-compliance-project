import request from '@/utils/request'

export default {
  listSeaArea() {
    return request.get('/compliance/seaArea/list')
  },
  listSpecies() {
    return request.get('/compliance/species/list')
  },
  queryBanRule(params) {
    return request.get('/compliance/banRule/query', { params })
  },
  listFishingGear() {
    return request.get('/compliance/fishingGear/list')
  },
  searchPolicy(keyword) {
    return request.get('/compliance/policy/search', { params: { keyword } })
  }
}