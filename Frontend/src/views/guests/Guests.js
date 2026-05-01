import React, { useEffect, useMemo, useState } from 'react'
import {
  CAlert,
  CBadge,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CForm,
  CFormInput,
  CFormLabel,
  CInputGroup,
  CInputGroupText,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CRow,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPencil, cilPlus, cilSearch, cilTrash } from '@coreui/icons'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../context/AuthContext'
import { guestApi } from '../../services/api'

const initialCreateForm = {
  firstName: '',
  lastName: '',
  phone: '',
  email: '',
  documentNumber: '',
}

const initialEditForm = {
  id: null,
  firstName: '',
  lastName: '',
  phone: '',
  email: '',
  documentNumber: '',
}

const Guests = () => {
  const { token } = useAuth()

  const [guests, setGuests] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [search, setSearch] = useState('')

  const [createVisible, setCreateVisible] = useState(false)
  const [editVisible, setEditVisible] = useState(false)

  const [createForm, setCreateForm] = useState(initialCreateForm)
  const [editForm, setEditForm] = useState(initialEditForm)

  const loadGuests = async (query = '') => {
    try {
      setLoading(true)
      setError('')

      const data = await guestApi.getAll(query ? { query } : {}, token)
      setGuests(data || [])
    } catch (err) {
      setError(err.message || 'Failed to load guests')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadGuests()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const filteredGuests = useMemo(() => {
    const q = search.trim().toLowerCase()

    if (!q) return guests

    return guests.filter((guest) => {
      return (
        guest.fullName?.toLowerCase().includes(q) ||
        guest.phone?.toLowerCase().includes(q) ||
        guest.email?.toLowerCase().includes(q) ||
        guest.documentNumber?.toLowerCase().includes(q)
      )
    })
  }, [guests, search])

  const openCreateModal = () => {
    setCreateForm(initialCreateForm)
    setCreateVisible(true)
  }

  const openEditModal = (guest) => {
    setEditForm({
      id: guest.id,
      firstName: guest.fullName?.split(' ')?.[0] || '',
      lastName: guest.fullName?.split(' ')?.slice(1).join(' ') || '',
      phone: guest.phone || '',
      email: guest.email || '',
      documentNumber: guest.documentNumber || '',
    })
    setEditVisible(true)
  }

  const handleCreateGuest = async (event) => {
    event.preventDefault()

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await guestApi.create(createForm, token)

      setSuccess('Guest created successfully')
      setCreateVisible(false)
      setCreateForm(initialCreateForm)
      await loadGuests(search)
    } catch (err) {
      setError(err.message || 'Failed to create guest')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateGuest = async (event) => {
    event.preventDefault()

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await guestApi.update(editForm.id, {
        firstName: editForm.firstName,
        lastName: editForm.lastName,
        phone: editForm.phone,
        email: editForm.email,
        documentNumber: editForm.documentNumber,
      }, token)

      setSuccess('Guest updated successfully')
      setEditVisible(false)
      setEditForm(initialEditForm)
      await loadGuests(search)
    } catch (err) {
      setError(err.message || 'Failed to update guest')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteGuest = async (guest) => {
    const confirmed = window.confirm(`Delete guest "${guest.fullName}"?`)
    if (!confirmed) return

    try {
      setError('')
      setSuccess('')

      await guestApi.remove(guest.id, token)

      setSuccess('Guest deleted successfully')
      await loadGuests(search)
    } catch (err) {
      setError(err.message || 'Failed to delete guest')
    }
  }

  const getInitials = (fullName) => {
    if (!fullName) return 'G'
    return fullName
      .split(' ')
      .map((part) => part[0])
      .join('')
      .slice(0, 2)
      .toUpperCase()
  }

  return (
    <>
      <PageHeader
        title="Guests"
        subtitle="Guest registry with search, create, edit, and soft delete."
        actionLabel="Add guest"
        onAction={openCreateModal}
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}
      {success ? <CAlert color="success">{success}</CAlert> : null}

      <CCard className="mb-4">
        <CCardHeader>Guest Registry</CCardHeader>
        <CCardBody>
          <CRow className="g-3 mb-4">
            <CCol md={8}>
              <CInputGroup>
                <CInputGroupText>
                  <CIcon icon={cilSearch} />
                </CInputGroupText>
                <CFormInput
                  placeholder="Search by name, phone, email, or document"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </CInputGroup>
            </CCol>

            <CCol md={4} className="d-flex gap-2">
              <CButton color="primary" onClick={() => loadGuests(search)}>
                Search
              </CButton>
              <CButton color="light" variant="outline" onClick={() => {
                setSearch('')
                loadGuests('')
              }}>
                Reset
              </CButton>
            </CCol>
          </CRow>

          {loading ? (
            <CSpinner color="primary" />
          ) : (
            <CTable responsive hover align="middle">
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Guest</CTableHeaderCell>
                  <CTableHeaderCell>Phone</CTableHeaderCell>
                  <CTableHeaderCell>Email</CTableHeaderCell>
                  <CTableHeaderCell>Document</CTableHeaderCell>
                  <CTableHeaderCell>Status</CTableHeaderCell>
                  <CTableHeaderCell className="text-end">Actions</CTableHeaderCell>
                </CTableRow>
              </CTableHead>

              <CTableBody>
                {filteredGuests.length ? (
                  filteredGuests.map((guest) => (
                    <CTableRow key={guest.id}>
                      <CTableDataCell>
                        <div className="d-flex align-items-center gap-3">
                          <div
                            className="rounded-circle d-flex align-items-center justify-content-center"
                            style={{
                              width: 36,
                              height: 36,
                              backgroundColor: '#e9ecef',
                              fontWeight: 600,
                            }}
                          >
                            {getInitials(guest.fullName)}
                          </div>
                          <div>
                            <div className="fw-semibold">{guest.fullName || '—'}</div>
                            <div className="text-medium-emphasis small">ID: {guest.id}</div>
                          </div>
                        </div>
                      </CTableDataCell>

                      <CTableDataCell>{guest.phone || '—'}</CTableDataCell>
                      <CTableDataCell>{guest.email || '—'}</CTableDataCell>
                      <CTableDataCell>{guest.documentNumber || '—'}</CTableDataCell>
                      <CTableDataCell>
                        <CBadge color={guest.active ? 'success' : 'danger'}>
                          {guest.active ? 'ACTIVE' : 'INACTIVE'}
                        </CBadge>
                      </CTableDataCell>

                      <CTableDataCell className="text-end">
                        <div className="d-flex gap-2 justify-content-end">
                          <CButton
                            size="sm"
                            color="info"
                            variant="outline"
                            onClick={() => openEditModal(guest)}
                          >
                            <CIcon icon={cilPencil} />
                          </CButton>

                          <CButton
                            size="sm"
                            color="danger"
                            variant="outline"
                            onClick={() => handleDeleteGuest(guest)}
                          >
                            <CIcon icon={cilTrash} />
                          </CButton>
                        </div>
                      </CTableDataCell>
                    </CTableRow>
                  ))
                ) : (
                  <CTableRow>
                    <CTableDataCell colSpan={6} className="text-center text-medium-emphasis">
                      No guests found.
                    </CTableDataCell>
                  </CTableRow>
                )}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      {/* CREATE MODAL */}
      <CModal visible={createVisible} onClose={() => setCreateVisible(false)}>
        <CModalHeader>
          <CModalTitle>Create Guest</CModalTitle>
        </CModalHeader>

        <CForm onSubmit={handleCreateGuest}>
          <CModalBody>
            <div className="mb-3">
              <CFormLabel>First name</CFormLabel>
              <CFormInput
                required
                value={createForm.firstName}
                onChange={(e) => setCreateForm((prev) => ({ ...prev, firstName: e.target.value }))}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Last name</CFormLabel>
              <CFormInput
                required
                value={createForm.lastName}
                onChange={(e) => setCreateForm((prev) => ({ ...prev, lastName: e.target.value }))}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Phone</CFormLabel>
              <CFormInput
                required
                value={createForm.phone}
                onChange={(e) => setCreateForm((prev) => ({ ...prev, phone: e.target.value }))}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Email</CFormLabel>
              <CFormInput
                required
                type="email"
                value={createForm.email}
                onChange={(e) => setCreateForm((prev) => ({ ...prev, email: e.target.value }))}
              />
            </div>

            <div>
              <CFormLabel>Document number</CFormLabel>
              <CFormInput
                value={createForm.documentNumber}
                onChange={(e) => setCreateForm((prev) => ({ ...prev, documentNumber: e.target.value }))}
              />
            </div>
          </CModalBody>

          <CModalFooter>
            <CButton color="secondary" onClick={() => setCreateVisible(false)}>
              Cancel
            </CButton>
            <CButton type="submit" color="primary" disabled={saving}>
              {saving ? 'Saving...' : 'Create'}
            </CButton>
          </CModalFooter>
        </CForm>
      </CModal>

      {/* EDIT MODAL */}
      <CModal visible={editVisible} onClose={() => setEditVisible(false)}>
        <CModalHeader>
          <CModalTitle>Edit Guest</CModalTitle>
        </CModalHeader>

        <CForm onSubmit={handleUpdateGuest}>
          <CModalBody>
            <div className="mb-3">
              <CFormLabel>First name</CFormLabel>
              <CFormInput
                required
                value={editForm.firstName}
                onChange={(e) => setEditForm((prev) => ({ ...prev, firstName: e.target.value }))}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Last name</CFormLabel>
              <CFormInput
                required
                value={editForm.lastName}
                onChange={(e) => setEditForm((prev) => ({ ...prev, lastName: e.target.value }))}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Phone</CFormLabel>
              <CFormInput
                required
                value={editForm.phone}
                onChange={(e) => setEditForm((prev) => ({ ...prev, phone: e.target.value }))}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>Email</CFormLabel>
              <CFormInput
                required
                type="email"
                value={editForm.email}
                onChange={(e) => setEditForm((prev) => ({ ...prev, email: e.target.value }))}
              />
            </div>

            <div>
              <CFormLabel>Document number</CFormLabel>
              <CFormInput
                value={editForm.documentNumber}
                onChange={(e) => setEditForm((prev) => ({ ...prev, documentNumber: e.target.value }))}
              />
            </div>
          </CModalBody>

          <CModalFooter>
            <CButton color="secondary" onClick={() => setEditVisible(false)}>
              Cancel
            </CButton>
            <CButton type="submit" color="primary" disabled={saving}>
              {saving ? 'Saving...' : 'Update'}
            </CButton>
          </CModalFooter>
        </CForm>
      </CModal>
    </>
  )
}

export default Guests