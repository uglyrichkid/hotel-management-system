import React, { useEffect, useMemo, useState } from 'react'
import {
  CAlert,
  CBadge,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CFormInput,
  CFormLabel,
  CFormSelect,
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
import {
  cilCheckCircle,
  cilPencil,
  cilPlus,
  cilSearch,
  cilXCircle,
} from '@coreui/icons'
import PageHeader from '../../components/common/PageHeader'
import { useAuth } from '../../context/AuthContext'
import { usersApi } from '../../services/api'

const initialCreateForm = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
  status: 'ACTIVE',
  roleIds: [],
  hotelIds: [],
}

const initialEditForm = {
  id: null,
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  status: 'ACTIVE',
  roleIds: [],
  hotelIds: [],
}

const Users = () => {
  const { token } = useAuth()

  const [users, setUsers] = useState([])
  const [roles, setRoles] = useState([])
  const [hotels, setHotels] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [roleFilter, setRoleFilter] = useState('')

  const [createVisible, setCreateVisible] = useState(false)
  const [editVisible, setEditVisible] = useState(false)

  const [createForm, setCreateForm] = useState(initialCreateForm)
  const [editForm, setEditForm] = useState(initialEditForm)

  const loadData = async () => {
    try {
      setLoading(true)
      setError('')

      const [usersData, rolesData, hotelsData] = await Promise.all([
        usersApi.getAll(token),
        usersApi.getRoles(token),
        usersApi.getHotels(token),
      ])

      setUsers(usersData || [])
      setRoles(rolesData || [])
      setHotels(hotelsData || [])
    } catch (err) {
      setError(err.message || 'Failed to load users data')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const filteredUsers = useMemo(() => {
    return users.filter((user) => {
      const q = search.trim().toLowerCase()

      const matchesSearch =
        !q ||
        user.fullName?.toLowerCase().includes(q) ||
        user.email?.toLowerCase().includes(q) ||
        user.phone?.toLowerCase?.().includes?.(q)

      const matchesStatus = !statusFilter || user.status === statusFilter
      const matchesRole = !roleFilter || (user.roles || []).includes(roleFilter)

      return matchesSearch && matchesStatus && matchesRole
    })
  }, [users, search, statusFilter, roleFilter])

  const resetCreateForm = () => {
    setCreateForm(initialCreateForm)
  }

  const resetEditForm = () => {
    setEditForm(initialEditForm)
  }

  const openCreateModal = () => {
    resetCreateForm()
    setCreateVisible(true)
  }

  const openEditModal = (user) => {
    const selectedRoleIds = (roles || [])
      .filter((role) => (user.roles || []).includes(role.name))
      .map((role) => role.id)

    setEditForm({
      id: user.id,
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      phone: user.phone || '',
      status: user.status || 'ACTIVE',
      roleIds: selectedRoleIds,
      hotelIds: user.hotelIds || [],
    })

    setEditVisible(true)
  }

  const handleCreate = async () => {
    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await usersApi.create(
        {
          firstName: createForm.firstName,
          lastName: createForm.lastName,
          email: createForm.email,
          phone: createForm.phone,
          password: createForm.password,
          status: createForm.status,
          roleIds: createForm.roleIds,
          hotelIds: createForm.hotelIds,
        },
        token,
      )

      setSuccess('User created successfully')
      setCreateVisible(false)
      resetCreateForm()
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to create user')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdate = async () => {
    try {
      setSaving(true)
      setError('')
      setSuccess('')

      await usersApi.update(
        editForm.id,
        {
          firstName: editForm.firstName,
          lastName: editForm.lastName,
          email: editForm.email,
          phone: editForm.phone,
          status: editForm.status,
          roleIds: editForm.roleIds,
          hotelIds: editForm.hotelIds,
        },
        token,
      )

      setSuccess('User updated successfully')
      setEditVisible(false)
      resetEditForm()
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to update user')
    } finally {
      setSaving(false)
    }
  }

  const handleToggleStatus = async (user) => {
    try {
      setError('')
      setSuccess('')

      const nextStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
      await usersApi.updateStatus(user.id, nextStatus, token)

      setSuccess(`User status changed to ${nextStatus}`)
      await loadData()
    } catch (err) {
      setError(err.message || 'Failed to update status')
    }
  }

  const badgeColor = (status) => {
    if (status === 'ACTIVE') return 'success'
    if (status === 'DISABLED' || status === 'INACTIVE') return 'danger'
    return 'secondary'
  }

  const toggleCreateRole = (roleId, checked) => {
    setCreateForm((prev) => ({
      ...prev,
      roleIds: checked
        ? [...prev.roleIds, roleId]
        : prev.roleIds.filter((id) => id !== roleId),
    }))
  }

  const toggleCreateHotel = (hotelId, checked) => {
    setCreateForm((prev) => ({
      ...prev,
      hotelIds: checked
        ? [...prev.hotelIds, hotelId]
        : prev.hotelIds.filter((id) => id !== hotelId),
    }))
  }

  const toggleEditRole = (roleId, checked) => {
    setEditForm((prev) => ({
      ...prev,
      roleIds: checked
        ? [...prev.roleIds, roleId]
        : prev.roleIds.filter((id) => id !== roleId),
    }))
  }

  const toggleEditHotel = (hotelId, checked) => {
    setEditForm((prev) => ({
      ...prev,
      hotelIds: checked
        ? [...prev.hotelIds, hotelId]
        : prev.hotelIds.filter((id) => id !== hotelId),
    }))
  }

  return (
    <>
      <PageHeader
        title="Users & Access"
        subtitle="Manage employees, roles, and hotel access."
      />

      {error ? <CAlert color="danger">{error}</CAlert> : null}
      {success ? <CAlert color="success">{success}</CAlert> : null}

      <CCard className="mb-4">
        <CCardHeader className="d-flex justify-content-between align-items-center">
          <span>Users</span>
          <CButton color="primary" onClick={openCreateModal}>
            <CIcon icon={cilPlus} className="me-2" />
            Add User
          </CButton>
        </CCardHeader>

        <CCardBody>
          <CRow className="g-3 mb-4">
            <CCol md={4}>
              <CInputGroup>
                <CInputGroupText>
                  <CIcon icon={cilSearch} />
                </CInputGroupText>
                <CFormInput
                  placeholder="Search by name, email, or phone"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </CInputGroup>
            </CCol>

            <CCol md={4}>
              <CFormSelect
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                options={[
                  { label: 'All statuses', value: '' },
                  { label: 'ACTIVE', value: 'ACTIVE' },
                  { label: 'DISABLED', value: 'DISABLED' },
                  { label: 'INACTIVE', value: 'INACTIVE' },
                ]}
              />
            </CCol>

            <CCol md={4}>
              <CFormSelect
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
                options={[
                  { label: 'All roles', value: '' },
                  ...roles.map((role) => ({
                    label: role.name,
                    value: role.name,
                  })),
                ]}
              />
            </CCol>
          </CRow>

          {loading ? (
            <CSpinner color="primary" />
          ) : (
            <CTable responsive hover align="middle">
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Name</CTableHeaderCell>
                  <CTableHeaderCell>Email</CTableHeaderCell>
                  <CTableHeaderCell>Phone</CTableHeaderCell>
                  <CTableHeaderCell>Status</CTableHeaderCell>
                  <CTableHeaderCell>Roles</CTableHeaderCell>
                  <CTableHeaderCell>Hotels</CTableHeaderCell>
                  <CTableHeaderCell className="text-end">Actions</CTableHeaderCell>
                </CTableRow>
              </CTableHead>

              <CTableBody>
                {filteredUsers.length ? (
                  filteredUsers.map((user) => (
                    <CTableRow key={user.id}>
                      <CTableDataCell>
                        <div className="fw-semibold">{user.fullName || '—'}</div>
                      </CTableDataCell>

                      <CTableDataCell>{user.email || '—'}</CTableDataCell>

                      <CTableDataCell>{user.phone || '—'}</CTableDataCell>

                      <CTableDataCell>
                        <CBadge color={badgeColor(user.status)}>
                          {user.status || '—'}
                        </CBadge>
                      </CTableDataCell>

                      <CTableDataCell>
                        {(user.roles || []).length ? user.roles.join(', ') : '—'}
                      </CTableDataCell>

                      <CTableDataCell>
                        {(user.hotelNames || []).length ? user.hotelNames.join(', ') : '—'}
                      </CTableDataCell>

                      <CTableDataCell className="text-end">
                        <div className="d-flex gap-2 justify-content-end">
                          <CButton
                            size="sm"
                            color="info"
                            variant="outline"
                            onClick={() => openEditModal(user)}
                          >
                            <CIcon icon={cilPencil} />
                          </CButton>

                          <CButton
                            size="sm"
                            color={user.status === 'ACTIVE' ? 'danger' : 'success'}
                            variant="outline"
                            onClick={() => handleToggleStatus(user)}
                          >
                            <CIcon
                              icon={user.status === 'ACTIVE' ? cilXCircle : cilCheckCircle}
                            />
                          </CButton>
                        </div>
                      </CTableDataCell>
                    </CTableRow>
                  ))
                ) : (
                  <CTableRow>
                    <CTableDataCell colSpan={7} className="text-center text-medium-emphasis">
                      No users found.
                    </CTableDataCell>
                  </CTableRow>
                )}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      <CModal visible={createVisible} onClose={() => setCreateVisible(false)} size="lg">
        <CModalHeader>
          <CModalTitle>Create User</CModalTitle>
        </CModalHeader>

        <CModalBody>
          <CRow className="g-3">
            <CCol md={6}>
              <CFormLabel>First name</CFormLabel>
              <CFormInput
                value={createForm.firstName}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, firstName: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Last name</CFormLabel>
              <CFormInput
                value={createForm.lastName}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, lastName: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Email</CFormLabel>
              <CFormInput
                type="email"
                value={createForm.email}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, email: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Phone</CFormLabel>
              <CFormInput
                value={createForm.phone}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, phone: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Password</CFormLabel>
              <CFormInput
                type="password"
                value={createForm.password}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, password: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Status</CFormLabel>
              <CFormSelect
                value={createForm.status}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, status: e.target.value }))
                }
                options={[
                  { label: 'ACTIVE', value: 'ACTIVE' },
                  { label: 'DISABLED', value: 'DISABLED' },
                ]}
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Roles</CFormLabel>
              <div
                className="border rounded p-3"
                style={{ maxHeight: '180px', overflowY: 'auto' }}
              >
                {roles.length ? (
                  roles.map((role) => (
                    <div key={role.id} className="form-check mb-2">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id={`create-role-${role.id}`}
                        checked={createForm.roleIds.includes(role.id)}
                        onChange={(e) => toggleCreateRole(role.id, e.target.checked)}
                      />
                      <label className="form-check-label" htmlFor={`create-role-${role.id}`}>
                        {role.name}
                      </label>
                    </div>
                  ))
                ) : (
                  <div className="text-medium-emphasis">No roles available</div>
                )}
              </div>
            </CCol>

            <CCol md={6}>
              <CFormLabel>Hotels</CFormLabel>
              <div
                className="border rounded p-3"
                style={{ maxHeight: '180px', overflowY: 'auto' }}
              >
                {hotels.length ? (
                  hotels.map((hotel) => (
                    <div key={hotel.id} className="form-check mb-2">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id={`create-hotel-${hotel.id}`}
                        checked={createForm.hotelIds.includes(hotel.id)}
                        onChange={(e) => toggleCreateHotel(hotel.id, e.target.checked)}
                      />
                      <label className="form-check-label" htmlFor={`create-hotel-${hotel.id}`}>
                        {hotel.name}
                      </label>
                    </div>
                  ))
                ) : (
                  <div className="text-medium-emphasis">No hotels available</div>
                )}
              </div>
            </CCol>
          </CRow>
        </CModalBody>

        <CModalFooter>
          <CButton color="secondary" onClick={() => setCreateVisible(false)}>
            Cancel
          </CButton>
          <CButton color="primary" onClick={handleCreate} disabled={saving}>
            {saving ? 'Saving...' : 'Create'}
          </CButton>
        </CModalFooter>
      </CModal>

      <CModal visible={editVisible} onClose={() => setEditVisible(false)} size="lg">
        <CModalHeader>
          <CModalTitle>Edit User</CModalTitle>
        </CModalHeader>

        <CModalBody>
          <CRow className="g-3">
            <CCol md={6}>
              <CFormLabel>First name</CFormLabel>
              <CFormInput
                value={editForm.firstName}
                onChange={(e) =>
                  setEditForm((prev) => ({ ...prev, firstName: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Last name</CFormLabel>
              <CFormInput
                value={editForm.lastName}
                onChange={(e) =>
                  setEditForm((prev) => ({ ...prev, lastName: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Email</CFormLabel>
              <CFormInput
                type="email"
                value={editForm.email}
                onChange={(e) =>
                  setEditForm((prev) => ({ ...prev, email: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Phone</CFormLabel>
              <CFormInput
                value={editForm.phone}
                onChange={(e) =>
                  setEditForm((prev) => ({ ...prev, phone: e.target.value }))
                }
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Status</CFormLabel>
              <CFormSelect
                value={editForm.status}
                onChange={(e) =>
                  setEditForm((prev) => ({ ...prev, status: e.target.value }))
                }
                options={[
                  { label: 'ACTIVE', value: 'ACTIVE' },
                  { label: 'DISABLED', value: 'DISABLED' },
                  { label: 'INACTIVE', value: 'INACTIVE' },
                ]}
              />
            </CCol>

            <CCol md={6}>
              <CFormLabel>Roles</CFormLabel>
              <div
                className="border rounded p-3"
                style={{ maxHeight: '180px', overflowY: 'auto' }}
              >
                {roles.length ? (
                  roles.map((role) => (
                    <div key={role.id} className="form-check mb-2">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id={`edit-role-${role.id}`}
                        checked={editForm.roleIds.includes(role.id)}
                        onChange={(e) => toggleEditRole(role.id, e.target.checked)}
                      />
                      <label className="form-check-label" htmlFor={`edit-role-${role.id}`}>
                        {role.name}
                      </label>
                    </div>
                  ))
                ) : (
                  <div className="text-medium-emphasis">No roles available</div>
                )}
              </div>
            </CCol>

            <CCol md={12}>
              <CFormLabel>Hotels</CFormLabel>
              <div
                className="border rounded p-3"
                style={{ maxHeight: '180px', overflowY: 'auto' }}
              >
                {hotels.length ? (
                  hotels.map((hotel) => (
                    <div key={hotel.id} className="form-check mb-2">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id={`edit-hotel-${hotel.id}`}
                        checked={editForm.hotelIds.includes(hotel.id)}
                        onChange={(e) => toggleEditHotel(hotel.id, e.target.checked)}
                      />
                      <label className="form-check-label" htmlFor={`edit-hotel-${hotel.id}`}>
                        {hotel.name}
                      </label>
                    </div>
                  ))
                ) : (
                  <div className="text-medium-emphasis">No hotels available</div>
                )}
              </div>
            </CCol>
          </CRow>
        </CModalBody>

        <CModalFooter>
          <CButton color="secondary" onClick={() => setEditVisible(false)}>
            Cancel
          </CButton>
          <CButton color="primary" onClick={handleUpdate} disabled={saving}>
            {saving ? 'Saving...' : 'Update'}
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default Users