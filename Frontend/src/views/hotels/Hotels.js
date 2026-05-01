import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CAlert,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormInput,
  CFormLabel,
  CFormSelect,
  CRow,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import PageHeader from '../../components/common/PageHeader'
import StatusBadge from '../../components/common/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { apiFetch, buildQuery } from '../../services/api'

const Hotels = () => {
  const { token, selectedHotelId, setSelectedHotelId } = useAuth()
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({
    name: '',
    status: '',
    stars: '',
    cityId: '',
  })
  const [hotels, setHotels] = useState([])
  const [cities, setCities] = useState([])

  const loadHotels = async () => {
    try {
      setLoading(true)
      setError('')
      const data = await apiFetch(`/api/hotels${buildQuery(filters)}`, {}, token)
      setHotels(data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const loadCities = async () => {
    try {
      const data = await apiFetch('/api/cities', {}, token)
      setCities(data || [])
    } catch (err) {
      console.log('Failed to load cities:', err.message)
    }
  }

  useEffect(() => {
    loadHotels()
    loadCities()
  }, [])

  const handleFilterSubmit = (event) => {
    event.preventDefault()
    loadHotels()
  }

  const handleManage = (hotelId) => {
    setSelectedHotelId(hotelId)
    navigate(`/hotels/${hotelId}/manage`)
  }

  const handleRowSelect = (hotelId, status) => {
    if (status === 'INACTIVE') return
    setSelectedHotelId(hotelId)
  }

  const handleDeactivate = async (id) => {
    if (!window.confirm('Deactivate this hotel?')) return
    try {
      await apiFetch(`/api/hotels/${id}`, { method: 'DELETE' }, token)
      loadHotels()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleActivate = async (hotelId) => {
    try {
      setError('')
      await apiFetch(
        `/api/hotels/${hotelId}/activate`,
        { method: 'PUT' },
        token,
      )
      loadHotels()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <>
      <PageHeader
        title="Hotels"
        subtitle="Manage hotel list and open detailed hotel management pages."
        actionLabel="Add hotel"
        onAction={() => navigate('/hotels/create')}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}

      <CCard className="mb-4">
        <CCardHeader>Filters</CCardHeader>
        <CCardBody>
          <CForm onSubmit={handleFilterSubmit}>
            <CRow className="g-3">
              <CCol md={3}>
                <CFormLabel>Name</CFormLabel>
                <CFormInput
                  value={filters.name}
                  onChange={(e) => setFilters({ ...filters, name: e.target.value })}
                  placeholder="Hotel name"
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>Status</CFormLabel>
                <CFormSelect
                  value={filters.status}
                  onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                >
                  <option value="">All</option>
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="INACTIVE">INACTIVE</option>
                </CFormSelect>
              </CCol>

              <CCol md={3}>
                <CFormLabel>Stars</CFormLabel>
                <CFormInput
                  value={filters.stars}
                  onChange={(e) => setFilters({ ...filters, stars: e.target.value })}
                  placeholder="Minimum stars"
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>City</CFormLabel>
                <CFormSelect
                  value={filters.cityId}
                  onChange={(e) => setFilters({ ...filters, cityId: e.target.value })}
                >
                  <option value="">All cities</option>
                  {cities.map((city) => (
                    <option key={city.id} value={city.id}>
                      {city.name}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={12} className="d-flex justify-content-end">
                <CButton color="primary" type="submit">
                  Apply filters
                </CButton>
              </CCol>
            </CRow>
          </CForm>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>Hotels</CCardHeader>
        <CCardBody>
          {loading ? <CSpinner color="primary" /> : null}

          <CTable responsive hover>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>ID</CTableHeaderCell>
                <CTableHeaderCell>Hotel Name</CTableHeaderCell>
                <CTableHeaderCell>City</CTableHeaderCell>
                <CTableHeaderCell>Stars</CTableHeaderCell>
                <CTableHeaderCell>Currency</CTableHeaderCell>
                <CTableHeaderCell>Rooms</CTableHeaderCell>
                <CTableHeaderCell>Director</CTableHeaderCell>
                <CTableHeaderCell>Status</CTableHeaderCell>
                <CTableHeaderCell>Actions</CTableHeaderCell>
              </CTableRow>
            </CTableHead>

            <CTableBody>
              {hotels.map((hotel) => (
                <CTableRow
                  key={hotel.id}
                  className={String(selectedHotelId) === String(hotel.id) ? 'table-primary' : ''}
                  style={{ cursor: hotel.status === 'INACTIVE' ? 'not-allowed' : 'pointer' }}
                  onClick={() => handleRowSelect(hotel.id, hotel.status)}
                >
                  <CTableDataCell>{hotel.id}</CTableDataCell>

                  <CTableDataCell>{hotel.name}</CTableDataCell>

                  <CTableDataCell>
                    {hotel.cityName ||
                      cities.find((c) => String(c.id) === String(hotel.cityId))?.name ||
                      '-'}
                  </CTableDataCell>

                  <CTableDataCell>
                    {[1, 2, 3, 4, 5].map((s) => (
                      <span key={s}>{s <= hotel.stars ? '⭐' : '☆'}</span>
                    ))}
                  </CTableDataCell>

                  <CTableDataCell>{hotel.currencyCode || '-'}</CTableDataCell>

                  <CTableDataCell>{hotel.roomsCount ?? '-'}</CTableDataCell>

                  <CTableDataCell>{hotel.directorName ?? '-'}</CTableDataCell>

                  <CTableDataCell>
                    <StatusBadge value={hotel.status} />
                  </CTableDataCell>

                  <CTableDataCell onClick={(e) => e.stopPropagation()}>
                    <div className="d-flex gap-2 flex-wrap">
                      <CButton
                        size="sm"
                        color="info"
                        variant="outline"
                        disabled={hotel.status === 'INACTIVE'}
                        onClick={() => handleManage(hotel.id)}
                      >
                        Manage
                      </CButton>

                      {hotel.status === 'INACTIVE' ? (
                        <CButton
                          size="sm"
                          color="success"
                          variant="outline"
                          onClick={() => handleActivate(hotel.id)}
                        >
                          Activate
                        </CButton>
                      ) : (
                        <CButton
                          size="sm"
                          color="danger"
                          variant="outline"
                          onClick={() => handleDeactivate(hotel.id)}
                        >
                          Deactivate
                        </CButton>
                      )}
                    </div>
                  </CTableDataCell>
                </CTableRow>
              ))}
            </CTableBody>
          </CTable>
        </CCardBody>
      </CCard>
    </>
  )
}

export default Hotels