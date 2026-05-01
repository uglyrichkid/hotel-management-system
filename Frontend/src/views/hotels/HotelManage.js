import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  CAlert,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormCheck,
  CFormInput,
  CFormLabel,
  CFormSelect,
  CRow,
  CSpinner,
  CFormTextarea,
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
import { apiFetch } from '../../services/api'

const currencyOptions = [
  { value: 'AMD', label: 'AMD' },
  { value: 'USD', label: 'USD' },
  { value: 'EUR', label: 'EUR' },
  { value: 'RUB', label: 'RUB' },
  { value: 'GEL', label: 'GEL' },
]

const HotelManage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const { token } = useAuth()

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [memberSaving, setMemberSaving] = useState(false)
  const [error, setError] = useState('')
  const [cities, setCities] = useState([])
  const [hotel, setHotel] = useState(null)

  const [form, setForm] = useState({
    name: '',
    address: '',
    cityId: '',
    stars: '3',
    status: 'ACTIVE',
    currencyCode: 'USD',
    description: '',
    checkInTime: '',
    checkOutTime: '',
    phone: '',
    email: '',
    website: '',
    policies: [],
    images: [],
  })

  const [memberForm, setMemberForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
    roleName: 'FRONT_DESK',
  })

  const loadCities = async () => {
    try {
      const data = await apiFetch('/api/cities', {}, token)
      setCities(data || [])
    } catch (err) {
      console.log('Failed to load cities:', err.message)
      setCities([])
    }
  }

  const loadHotel = async () => {
    const data = await apiFetch(`/api/hotels/${id}/manage`, {}, token)
    setHotel(data)

    setForm({
      name: data?.name || '',
      address: data?.address || '',
      cityId: String(data?.cityId || ''),
      stars: String(data?.stars || 3),
      status: data?.status || 'ACTIVE',
      currencyCode: data?.currencyCode || 'USD',
      description: data?.description || '',
      checkInTime: data?.checkInTime || '',
      checkOutTime: data?.checkOutTime || '',
      phone: data?.phone || '',
      email: data?.email || '',
      website: data?.website || '',
      policies: Array.isArray(data?.policies) ? data.policies : [],
      images: Array.isArray(data?.images) ? data.images : [],
    })
  }

  const loadData = async () => {
    try {
      setLoading(true)
      setError('')
      await Promise.all([loadCities(), loadHotel()])
    } catch (err) {
      console.error(err)
      setError(err.message || 'Failed to load hotel management page')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [id])

  const handlePolicyChange = (index, field, value) => {
    const updated = [...form.policies]
    updated[index] = { ...updated[index], [field]: value }
    setForm({ ...form, policies: updated })
  }

  const handleAddPolicy = () => {
    setForm({
      ...form,
      policies: [...form.policies, { id: null, policyType: '', text: '' }],
    })
  }

  const handleRemovePolicy = (index) => {
    const updated = form.policies.filter((_, i) => i !== index)
    setForm({ ...form, policies: updated })
  }

  const handleImageChange = (index, field, value) => {
    const updated = [...form.images]
    updated[index] = { ...updated[index], [field]: value }
    setForm({ ...form, images: updated })
  }

  const handleAddImage = () => {
    setForm({
      ...form,
      images: [...form.images, { id: null, url: '', isMain: false, sortOrder: '' }],
    })
  }

  const handleRemoveImage = (index) => {
    const updated = form.images.filter((_, i) => i !== index)
    setForm({ ...form, images: updated })
  }

  const handleSave = async (e) => {
    e.preventDefault()

    try {
      setSaving(true)
      setError('')

      await apiFetch(
        `/api/hotels/${id}`,
        {
          method: 'PUT',
          body: JSON.stringify({
            name: form.name.trim(),
            address: form.address.trim(),
            cityId: Number(form.cityId),
            stars: Number(form.stars),
            status: form.status,
            currencyCode: form.currencyCode,
            description: form.description,
            checkInTime: form.checkInTime || null,
            checkOutTime: form.checkOutTime || null,
            phone: form.phone,
            email: form.email,
            website: form.website,
            policies: form.policies.map((p) => ({
              id: p.id ?? null,
              policyType: p.policyType,
              text: p.text,
            })),
            images: form.images.map((img) => ({
              id: img.id ?? null,
              url: img.url,
              isMain: !!img.isMain,
              sortOrder: img.sortOrder ? Number(img.sortOrder) : null,
            })),
          }),
        },
        token,
      )

      await loadHotel()
    } catch (err) {
      console.error(err)
      setError(err.message || 'Failed to save hotel')
    } finally {
      setSaving(false)
    }
  }

  const handleMemberChange = (field, value) => {
    setMemberForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleAddMember = async () => {
    try {
      setMemberSaving(true)
      setError('')

      await apiFetch(
        `/api/hotels/${id}/members`,
        {
          method: 'POST',
          body: JSON.stringify({
            firstName: memberForm.firstName.trim(),
            lastName: memberForm.lastName.trim(),
            email: memberForm.email.trim(),
            password: memberForm.password,
            phone: memberForm.phone,
            roleName: memberForm.roleName,
            status: 'ACTIVE',
          }),
        },
        token,
      )

      setMemberForm({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        phone: '',
        roleName: 'FRONT_DESK',
      })

      await loadHotel()
    } catch (err) {
      setError(err.message || 'Failed to add member')
    } finally {
      setMemberSaving(false)
    }
  }

  if (loading) {
    return <CSpinner color="primary" />
  }

  if (error && !hotel) {
    return <CAlert color="danger">{error}</CAlert>
  }

  if (!hotel) {
    return <CAlert color="warning">Hotel data was not loaded.</CAlert>
  }

  return (
    <>
      <PageHeader
        title={`${hotel.name}`}
        subtitle="Update hotel data, contacts, policies, images, and team."
        actionLabel="Back"
        onAction={() => navigate('/hotels')}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}

      <CForm onSubmit={handleSave}>
        <CCard className="mb-4">
          <CCardHeader className="d-flex justify-content-between align-items-center">
            <span>Hotel Details</span>
            <StatusBadge value={hotel.status} />
          </CCardHeader>

          <CCardBody>
            <CRow className="g-3">
              <CCol md={6}>
                <CFormLabel>Name</CFormLabel>
                <CFormInput
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Address</CFormLabel>
                <CFormInput
                  value={form.address}
                  onChange={(e) => setForm({ ...form, address: e.target.value })}
                />
              </CCol>

              <CCol md={3}>
                <CFormLabel>City</CFormLabel>
                <CFormSelect
                  value={form.cityId}
                  onChange={(e) => setForm({ ...form, cityId: e.target.value })}
                >
                  <option value="">Select city</option>
                  {cities.map((city) => (
                    <option key={city.id} value={city.id}>
                      {city.name}
                    </option>
                  ))}
                </CFormSelect>
              </CCol>

              <CCol md={3}>
                <CFormLabel>Stars</CFormLabel>
                <CFormSelect
                  value={form.stars}
                  onChange={(e) => setForm({ ...form, stars: e.target.value })}
                >
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                  <option value="5">5</option>
                </CFormSelect>
              </CCol>

              <CCol md={3}>
                <CFormLabel>Status</CFormLabel>
                <CFormSelect
                  value={form.status}
                  onChange={(e) => setForm({ ...form, status: e.target.value })}
                >
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="INACTIVE">INACTIVE</option>
                </CFormSelect>
              </CCol>

              <CCol md={3}>
                <CFormLabel>Currency</CFormLabel>
                <CFormSelect
                  value={form.currencyCode}
                  onChange={(e) => setForm({ ...form, currencyCode: e.target.value })}
                  options={currencyOptions}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Phone</CFormLabel>
                <CFormInput
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Email</CFormLabel>
                <CFormInput
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </CCol>

              <CCol md={4}>
                <CFormLabel>Website</CFormLabel>
                <CFormInput
                  value={form.website}
                  onChange={(e) => setForm({ ...form, website: e.target.value })}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Check-in Time</CFormLabel>
                <CFormInput
                  type="time"
                  value={form.checkInTime}
                  onChange={(e) => setForm({ ...form, checkInTime: e.target.value })}
                />
              </CCol>

              <CCol md={6}>
                <CFormLabel>Check-out Time</CFormLabel>
                <CFormInput
                  type="time"
                  value={form.checkOutTime}
                  onChange={(e) => setForm({ ...form, checkOutTime: e.target.value })}
                />
              </CCol>

              <CCol md={12}>
                <CFormLabel>Description</CFormLabel>
                <CFormTextarea
                  rows={4}
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                />
              </CCol>
            </CRow>
          </CCardBody>
        </CCard>

        <CCard className="mb-4">
          <CCardHeader className="d-flex justify-content-between align-items-center">
            <span>Policies</span>
            <CButton size="sm" color="primary" variant="outline" onClick={handleAddPolicy}>
              Add policy
            </CButton>
          </CCardHeader>
          <CCardBody>
            <CRow className="g-3">
              {form.policies.map((policy, index) => (
                <React.Fragment key={index}>
                  <CCol md={4}>
                    <CFormLabel>Policy type</CFormLabel>
                    <CFormInput
                      value={policy.policyType || ''}
                      onChange={(e) => handlePolicyChange(index, 'policyType', e.target.value)}
                    />
                  </CCol>
                  <CCol md={7}>
                    <CFormLabel>Text</CFormLabel>
                    <CFormInput
                      value={policy.text || ''}
                      onChange={(e) => handlePolicyChange(index, 'text', e.target.value)}
                    />
                  </CCol>
                  <CCol md={1} className="d-flex align-items-end">
                    <CButton color="danger" variant="outline" onClick={() => handleRemovePolicy(index)}>
                      Remove
                    </CButton>
                  </CCol>
                </React.Fragment>
              ))}
              {!form.policies.length && <CCol xs={12}>No policies added.</CCol>}
            </CRow>
          </CCardBody>
        </CCard>

        <CCard className="mb-4">
          <CCardHeader className="d-flex justify-content-between align-items-center">
            <span>Images</span>
            <CButton size="sm" color="primary" variant="outline" onClick={handleAddImage}>
              Add image
            </CButton>
          </CCardHeader>
          <CCardBody>
            <CRow className="g-3">
              {form.images.map((image, index) => (
                <React.Fragment key={index}>
                  <CCol md={7}>
                    <CFormLabel>Image URL</CFormLabel>
                    <CFormInput
                      value={image.url || ''}
                      onChange={(e) => handleImageChange(index, 'url', e.target.value)}
                    />
                  </CCol>
                  <CCol md={2}>
                    <CFormLabel>Sort order</CFormLabel>
                    <CFormInput
                      value={image.sortOrder ?? ''}
                      onChange={(e) => handleImageChange(index, 'sortOrder', e.target.value)}
                    />
                  </CCol>
                  <CCol md={2} className="d-flex align-items-end">
                    <CFormCheck
                      label="Main"
                      checked={!!image.isMain}
                      onChange={(e) => handleImageChange(index, 'isMain', e.target.checked)}
                    />
                  </CCol>
                  <CCol md={1} className="d-flex align-items-end">
                    <CButton color="danger" variant="outline" onClick={() => handleRemoveImage(index)}>
                      Remove
                    </CButton>
                  </CCol>
                </React.Fragment>
              ))}
              {!form.images.length && <CCol xs={12}>No images added.</CCol>}
            </CRow>
          </CCardBody>
        </CCard>

        <div className="d-flex justify-content-end gap-2 mb-4">
          <CButton color="light" onClick={() => navigate('/hotels')}>
            Cancel
          </CButton>
          <CButton color="primary" type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save Changes'}
          </CButton>
        </div>
      </CForm>

      <CCard className="mb-4">
        <CCardHeader>Add Team Member</CCardHeader>
        <CCardBody>
          <CRow className="g-3">
            <CCol md={3}>
              <CFormLabel>First Name</CFormLabel>
              <CFormInput
                value={memberForm.firstName}
                onChange={(e) => handleMemberChange('firstName', e.target.value)}
              />
            </CCol>

            <CCol md={3}>
              <CFormLabel>Last Name</CFormLabel>
              <CFormInput
                value={memberForm.lastName}
                onChange={(e) => handleMemberChange('lastName', e.target.value)}
              />
            </CCol>

            <CCol md={2}>
              <CFormLabel>Phone</CFormLabel>
              <CFormInput
                value={memberForm.phone}
                onChange={(e) => handleMemberChange('phone', e.target.value)}
              />
            </CCol>

            <CCol md={2}>
              <CFormLabel>Role</CFormLabel>
              <CFormSelect
                value={memberForm.roleName}
                onChange={(e) => handleMemberChange('roleName', e.target.value)}
              >
                <option value="OPERATIONS_MANAGER">MANAGER</option>
                <option value="FRONT_DESK">RECEPTIONIST</option>
              </CFormSelect>
            </CCol>

            <CCol md={2}>
              <CFormLabel>Email</CFormLabel>
              <CFormInput
                value={memberForm.email}
                onChange={(e) => handleMemberChange('email', e.target.value)}
              />
            </CCol>

            <CCol md={3}>
              <CFormLabel>Password</CFormLabel>
              <CFormInput
                type="password"
                value={memberForm.password}
                onChange={(e) => handleMemberChange('password', e.target.value)}
              />
            </CCol>

            <CCol md={12} className="d-flex justify-content-end">
              <CButton color="primary" onClick={handleAddMember} disabled={memberSaving}>
                {memberSaving ? 'Adding...' : 'Add Member'}
              </CButton>
            </CCol>
          </CRow>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>Team Members</CCardHeader>
        <CCardBody>
          <CTable responsive hover>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>Name</CTableHeaderCell>
                <CTableHeaderCell>Email</CTableHeaderCell>
                <CTableHeaderCell>Phone</CTableHeaderCell>
                <CTableHeaderCell>Roles</CTableHeaderCell>
                <CTableHeaderCell>Status</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {(hotel.members || []).map((member) => (
                <CTableRow key={member.id}>
                  <CTableDataCell>{member.fullName || '-'}</CTableDataCell>
                  <CTableDataCell>{member.email || '-'}</CTableDataCell>
                  <CTableDataCell>{member.phone || '-'}</CTableDataCell>
                  <CTableDataCell>{(member.roles || []).join(', ') || '-'}</CTableDataCell>
                  <CTableDataCell>
                    <StatusBadge value={member.status} />
                  </CTableDataCell>
                </CTableRow>
              ))}
              {!hotel.members?.length && (
                <CTableRow>
                  <CTableDataCell colSpan={5}>No team members.</CTableDataCell>
                </CTableRow>
              )}
            </CTableBody>
          </CTable>
        </CCardBody>
      </CCard>
    </>
  )
}

export default HotelManage