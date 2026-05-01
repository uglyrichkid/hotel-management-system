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
  CFormTextarea,
  CRow,
  CSpinner,
} from '@coreui/react'

import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../context/AuthContext'
import { apiFetch } from '../../services/api'

const currencyOptions = [
  { value: 'AMD', label: 'AMD' },
  { value: 'USD', label: 'USD' },
  { value: 'EUR', label: 'EUR' },
  { value: 'RUB', label: 'RUB' },
  { value: 'GEL', label: 'GEL' },
]

const HotelCreate = () => {
  const navigate = useNavigate()
  const { token } = useAuth()

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [cities, setCities] = useState([])

  const [form, setForm] = useState({
    name: '',
    address: '',
    cityId: '',
    stars: '3',
    currencyCode: 'USD',
    description: '',
    checkInTime: '',
    checkOutTime: '',
    phone: '',
    email: '',
    website: '',
    director: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      phone: '',
    },
  })

  const loadCities = async () => {
    try {
      setLoading(true)
      const data = await apiFetch('/api/cities', {}, token)
      setCities(data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCities()
  }, [])

  const handleChange = (field, value) => {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleDirectorChange = (field, value) => {
    setForm((prev) => ({
      ...prev,
      director: {
        ...prev.director,
        [field]: value,
      },
    }))
  }

  const handleCreate = async (e) => {
    e.preventDefault()

    try {
      setSaving(true)
      setError('')

      const res = await apiFetch(
        '/api/hotels',
        {
          method: 'POST',
          body: JSON.stringify({
            name: form.name.trim(),
            address: form.address.trim(),
            cityId: Number(form.cityId),
            stars: Number(form.stars),
            currencyCode: form.currencyCode,
            description: form.description,
            checkInTime: form.checkInTime || null,
            checkOutTime: form.checkOutTime || null,
            phone: form.phone,
            email: form.email,
            website: form.website,
            director: {
              firstName: form.director.firstName.trim(),
              lastName: form.director.lastName.trim(),
              email: form.director.email.trim(),
              password: form.director.password,
              phone: form.director.phone,
            },
          }),
        },
        token,
      )

      if (res?.id) {
        navigate(`/hotels/${res.id}/manage`)
      } else {
        navigate('/hotels')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <CSpinner color="primary" />
  }

  return (
    <>
      <PageHeader
        title="Create Hotel"
        subtitle="Create a new hotel and its director account"
        actionLabel="Back"
        onAction={() => navigate('/hotels')}
      />

      {error && <CAlert color="danger">{error}</CAlert>}

      <CForm onSubmit={handleCreate}>
        <CCard className="mb-4">
          <CCardHeader>New Hotel</CCardHeader>
          <CCardBody>
            <CRow className="g-3">
              <CCol md={6}>
                <CFormLabel>Name</CFormLabel>
                <CFormInput
                  value={form.name}
                  onChange={(e) => handleChange('name', e.target.value)}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Address</CFormLabel>
                <CFormInput
                  value={form.address}
                  onChange={(e) => handleChange('address', e.target.value)}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>City</CFormLabel>
                <CFormSelect
                  value={form.cityId}
                  onChange={(e) => handleChange('cityId', e.target.value)}
                >
                  <option value="">Select city</option>
                  {cities.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={4}>
                <CFormLabel>Stars</CFormLabel>
                <CFormSelect
                  value={form.stars}
                  onChange={(e) => handleChange('stars', e.target.value)}
                >
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                  <option value="5">5</option>
                </CFormSelect>
              </CCol>

              <CCol md={4}>
                <CFormLabel>Currency</CFormLabel>
                <CFormSelect
                  value={form.currencyCode}
                  onChange={(e) => handleChange('currencyCode', e.target.value)}
                  options={currencyOptions}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Phone</CFormLabel>
                <CFormInput
                  value={form.phone}
                  onChange={(e) => handleChange('phone', e.target.value)}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Email</CFormLabel>
                <CFormInput
                  value={form.email}
                  onChange={(e) => handleChange('email', e.target.value)}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Website</CFormLabel>
                <CFormInput
                  value={form.website}
                  onChange={(e) => handleChange('website', e.target.value)}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Check-in Time</CFormLabel>
                <CFormInput
                  type="time"
                  value={form.checkInTime}
                  onChange={(e) => handleChange('checkInTime', e.target.value)}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Check-out Time</CFormLabel>
                <CFormInput
                  type="time"
                  value={form.checkOutTime}
                  onChange={(e) => handleChange('checkOutTime', e.target.value)}
                />
              </CCol>

              <CCol md={12}>
                <CFormLabel>Description</CFormLabel>
                <CFormTextarea
                  rows={4}
                  value={form.description}
                  onChange={(e) => handleChange('description', e.target.value)}
                />
              </CCol>
            </CRow>
          </CCardBody>
        </CCard>

        <CCard className="mb-4">
          <CCardHeader>Director Account</CCardHeader>
          <CCardBody>
            <CRow className="g-3">
              <CCol md={6}>
                <CFormLabel>First Name</CFormLabel>
                <CFormInput
                  value={form.director.firstName}
                  onChange={(e) => handleDirectorChange('firstName', e.target.value)}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Last Name</CFormLabel>
                <CFormInput
                  value={form.director.lastName}
                  onChange={(e) => handleDirectorChange('lastName', e.target.value)}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Email</CFormLabel>
                <CFormInput
                  type="email"
                  value={form.director.email}
                  onChange={(e) => handleDirectorChange('email', e.target.value)}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Password</CFormLabel>
                <CFormInput
                  type="password"
                  value={form.director.password}
                  onChange={(e) => handleDirectorChange('password', e.target.value)}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Phone</CFormLabel>
                <CFormInput
                  value={form.director.phone}
                  onChange={(e) => handleDirectorChange('phone', e.target.value)}
                />
              </CCol>
            </CRow>
          </CCardBody>
        </CCard>

        <div className="d-flex justify-content-end gap-2">
          <CButton color="light" onClick={() => navigate('/hotels')}>
            Cancel
          </CButton>
          <CButton color="primary" type="submit" disabled={saving}>
            {saving ? 'Creating...' : 'Create Hotel'}
          </CButton>
        </div>
      </CForm>
    </>
  )
}

export default HotelCreate