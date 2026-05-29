import request from './request'

export function getPositions(params) {
  return request.get('/positions', { params })
}

export function getPositionById(id) {
  return request.get(`/positions/${id}`)
}

export function createPosition(data) {
  return request.post('/positions', data)
}

export function updatePosition(id, data) {
  return request.put(`/positions/${id}`, data)
}

export function deletePosition(id) {
  return request.delete(`/positions/${id}`)
}

export function uploadExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/positions/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadTemplate() {
  return request.get('/positions/template', {
    responseType: 'blob'
  })
}

export function submitForReview(id) {
  return request.post(`/positions/${id}/submit`)
}

export function approvePosition(id) {
  return request.post(`/positions/${id}/approve`)
}

export function rejectPosition(id) {
  return request.post(`/positions/${id}/reject`)
}

export function getStatistics() {
  return request.get('/positions/stats')
}

export function autoCloseExpired() {
  return request.post('/positions/auto-close')
}
