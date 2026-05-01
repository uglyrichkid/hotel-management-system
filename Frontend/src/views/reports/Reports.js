import React, { useEffect, useMemo, useState } from 'react'
import {
  CAlert,
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CFormInput,
  CNav,
  CNavItem,
  CNavLink,
  CRow,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CWidgetStatsF,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilReload } from '@coreui/icons'
import PageHeader from '../../components/common/PageHeader'
import SelectedHotelAlert from '../../components/common/SelectedHotelAlert'
import { useAuth } from '../../context/AuthContext'
import { reportsApi } from '../../services/api'

const money = (value) => {
  const num = Number(value || 0)
  return num.toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const moneyWithCurrency = (value, currencyCode) => {
  const formatted = money(value)
  return currencyCode ? `${formatted} ${currencyCode}` : formatted
}

const formatDate = (value) => value || '—'

const getDefaultDateRange = () => {
  const today = new Date()
  const to = today.toISOString().slice(0, 10)
  const fromDate = new Date(today)
  fromDate.setDate(today.getDate() - 29)
  const from = fromDate.toISOString().slice(0, 10)
  return { from, to }
}

const BreakdownListCard = ({
  title,
  items = [],
  amountMode = false,
  currencyCode = '',
}) => (
  <CCard className="mb-4">
    <CCardHeader>{title}</CCardHeader>
    <CCardBody>
      {items.length ? (
        items.map((item, index) => (
          <div
            key={`${item.label}-${index}`}
            className="d-flex justify-content-between py-2 border-bottom"
          >
            <span>{item.label}</span>
            <strong>
              {amountMode
                ? moneyWithCurrency(item.amount, currencyCode)
                : item.count}
            </strong>
          </div>
        ))
      ) : (
        <div className="text-medium-emphasis">No data</div>
      )}
    </CCardBody>
  </CCard>
)

const Reports = () => {
  const { token, selectedHotelId } = useAuth()
  const defaultRange = useMemo(() => getDefaultDateRange(), [])

  const [activeTab, setActiveTab] = useState('bookings')
  const [dateFrom, setDateFrom] = useState(defaultRange.from)
  const [dateTo, setDateTo] = useState(defaultRange.to)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [data, setData] = useState({
    bookings: null,
    revenue: null,
    occupancy: null,
  })

  const loadReports = async () => {
    if (!selectedHotelId) return

    try {
      setLoading(true)
      setError('')

      const params = {
        hotelId: selectedHotelId,
        dateFrom,
        dateTo,
      }

      const [bookings, revenue, occupancy] = await Promise.all([
        reportsApi.getBookings(params, token),
        reportsApi.getRevenue(params, token),
        reportsApi.getOccupancy(params, token),
      ])

      setData({ bookings, revenue, occupancy })
    } catch (err) {
      setError(err.message || 'Failed to load reports')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!selectedHotelId) return
    loadReports()
  }, [selectedHotelId])

  if (!selectedHotelId) {
    return (
      <>
        <PageHeader title="Reports" subtitle="Operational and financial reports by selected hotel." />
        <SelectedHotelAlert />
      </>
    )
  }

  const { bookings, revenue, occupancy } = data
  const bookingsCurrency = bookings?.currencyCode || ''
  const revenueCurrency = revenue?.currencyCode || bookings?.currencyCode || ''

  return (
    <>
      <PageHeader
        title="Reports"
        subtitle="Bookings, revenue and occupancy reports for the selected hotel."
      />

      <CCard className="mb-4">
        <CCardBody>
          <CRow className="g-3">
            <CCol md={4}>
              <CFormInput
                type="date"
                label="From"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
              />
            </CCol>
            <CCol md={4}>
              <CFormInput
                type="date"
                label="To"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
              />
            </CCol>
            <CCol md={4} className="d-flex align-items-end gap-2">
              <CButton color="primary" onClick={loadReports}>
                <CIcon icon={cilReload} className="me-2" />
                Refresh
              </CButton>
            </CCol>
          </CRow>
        </CCardBody>
      </CCard>

      <CNav variant="tabs" className="mb-4">
        {['bookings', 'revenue', 'occupancy'].map((tab) => (
          <CNavItem key={tab}>
            <CNavLink
              active={activeTab === tab}
              href=""
              onClick={(e) => {
                e.preventDefault()
                setActiveTab(tab)
              }}
            >
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </CNavLink>
          </CNavItem>
        ))}
      </CNav>

      {error ? <CAlert color="danger">{error}</CAlert> : null}
      {loading ? <CSpinner color="primary" /> : null}

      {activeTab === 'bookings' && bookings ? (
        <>
          <CRow className="g-4 mb-4">
            <CCol md={3}>
              <CWidgetStatsF title="Total bookings" value={bookings.totalBookings ?? 0} />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF title="Confirmed" value={bookings.confirmedBookings ?? 0} />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF title="Checked in" value={bookings.checkedInBookings ?? 0} />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF title="Cancelled" value={bookings.cancelledBookings ?? 0} />
            </CCol>
          </CRow>

          <CRow className="g-4 mb-4">
            <CCol md={6}>
              <CWidgetStatsF
                title="Average booking value"
                value={moneyWithCurrency(bookings.averageBookingValue, bookingsCurrency)}
              />
            </CCol>
            <CCol md={6}>
              <CWidgetStatsF
                title="Average stay length"
                value={bookings.averageStayLength ?? 0}
              />
            </CCol>
          </CRow>

          <CRow>
            <CCol xl={6}>
              <BreakdownListCard
                title="Status breakdown"
                items={bookings.statusBreakdown || []}
              />
            </CCol>
            <CCol xl={6}>
              <BreakdownListCard
                title="Room type breakdown"
                items={bookings.roomTypeBreakdown || []}
              />
            </CCol>
          </CRow>

          <CCard>
            <CCardHeader>Booking rows</CCardHeader>
            <CCardBody>
              <CTable responsive hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>ID</CTableHeaderCell>
                    <CTableHeaderCell>Guest</CTableHeaderCell>
                    <CTableHeaderCell>Rooms</CTableHeaderCell>
                    <CTableHeaderCell>Check-in</CTableHeaderCell>
                    <CTableHeaderCell>Check-out</CTableHeaderCell>
                    <CTableHeaderCell>Status</CTableHeaderCell>
                    <CTableHeaderCell>Total</CTableHeaderCell>
                    <CTableHeaderCell>Paid</CTableHeaderCell>
                    <CTableHeaderCell>Remaining</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {(bookings.rows || []).map((row) => {
                    const rowCurrency = row.currencyCode || bookingsCurrency
                    return (
                      <CTableRow key={row.bookingId}>
                        <CTableDataCell>#{row.bookingId}</CTableDataCell>
                        <CTableDataCell>{row.guestName || '—'}</CTableDataCell>
                        <CTableDataCell>{row.rooms || '—'}</CTableDataCell>
                        <CTableDataCell>{formatDate(row.checkInDate)}</CTableDataCell>
                        <CTableDataCell>{formatDate(row.checkOutDate)}</CTableDataCell>
                        <CTableDataCell>{row.status || '—'}</CTableDataCell>
                        <CTableDataCell>
                          {moneyWithCurrency(row.totalAmount, rowCurrency)}
                        </CTableDataCell>
                        <CTableDataCell>
                          {moneyWithCurrency(row.paidAmount, rowCurrency)}
                        </CTableDataCell>
                        <CTableDataCell>
                          {moneyWithCurrency(row.remainingAmount, rowCurrency)}
                        </CTableDataCell>
                      </CTableRow>
                    )
                  })}

                  {!loading && (!bookings.rows || bookings.rows.length === 0) ? (
                    <CTableRow>
                      <CTableDataCell colSpan={9} className="text-center text-body-secondary py-4">
                        No booking rows found for selected period.
                      </CTableDataCell>
                    </CTableRow>
                  ) : null}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>
        </>
      ) : null}

      {activeTab === 'revenue' && revenue ? (
        <>
          <CRow className="g-4 mb-4">
            <CCol md={3}>
              <CWidgetStatsF
                title="Expected revenue"
                value={moneyWithCurrency(revenue.expectedRevenue, revenueCurrency)}
              />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF
                title="Collected revenue"
                value={moneyWithCurrency(revenue.collectedRevenue, revenueCurrency)}
              />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF
                title="Outstanding balance"
                value={moneyWithCurrency(revenue.outstandingBalance, revenueCurrency)}
              />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF
                title="Completed payments"
                value={revenue.completedPaymentsCount ?? 0}
              />
            </CCol>
          </CRow>

          <CRow className="g-4 mb-4">
            <CCol md={6}>
              <CWidgetStatsF
                title="Partial bookings"
                value={revenue.partialBookingsCount ?? 0}
              />
            </CCol>
            <CCol md={6}>
              <CWidgetStatsF
                title="Unpaid bookings"
                value={revenue.unpaidBookingsCount ?? 0}
              />
            </CCol>
          </CRow>

          <BreakdownListCard
            title="Payment method breakdown"
            items={revenue.paymentMethodBreakdown || []}
            amountMode
            currencyCode={revenueCurrency}
          />

          <CCard>
            <CCardHeader>Revenue rows</CCardHeader>
            <CCardBody>
              <CTable responsive hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Booking</CTableHeaderCell>
                    <CTableHeaderCell>Guest</CTableHeaderCell>
                    <CTableHeaderCell>Total</CTableHeaderCell>
                    <CTableHeaderCell>Paid</CTableHeaderCell>
                    <CTableHeaderCell>Remaining</CTableHeaderCell>
                    <CTableHeaderCell>Payment status</CTableHeaderCell>
                    <CTableHeaderCell>Methods</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {(revenue.revenueRows || []).map((row) => {
                    const rowCurrency = row.currencyCode || revenueCurrency
                    return (
                      <CTableRow key={row.bookingId}>
                        <CTableDataCell>#{row.bookingId}</CTableDataCell>
                        <CTableDataCell>{row.guestName || '—'}</CTableDataCell>
                        <CTableDataCell>
                          {moneyWithCurrency(row.totalAmount, rowCurrency)}
                        </CTableDataCell>
                        <CTableDataCell>
                          {moneyWithCurrency(row.paidAmount, rowCurrency)}
                        </CTableDataCell>
                        <CTableDataCell>
                          {moneyWithCurrency(row.remainingAmount, rowCurrency)}
                        </CTableDataCell>
                        <CTableDataCell>{row.paymentStatus || '—'}</CTableDataCell>
                        <CTableDataCell>{row.paymentMethods || '—'}</CTableDataCell>
                      </CTableRow>
                    )
                  })}

                  {!loading && (!revenue.revenueRows || revenue.revenueRows.length === 0) ? (
                    <CTableRow>
                      <CTableDataCell colSpan={7} className="text-center text-body-secondary py-4">
                        No revenue rows found for selected period.
                      </CTableDataCell>
                    </CTableRow>
                  ) : null}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>
        </>
      ) : null}

      {activeTab === 'occupancy' && occupancy ? (
        <>
          <CRow className="g-4 mb-4">
            <CCol md={3}>
              <CWidgetStatsF title="Total rooms" value={occupancy.totalRooms ?? 0} />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF title="Occupied rooms" value={occupancy.occupiedRooms ?? 0} />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF title="Available rooms" value={occupancy.availableRooms ?? 0} />
            </CCol>
            <CCol md={3}>
              <CWidgetStatsF title="Out of service" value={occupancy.outOfServiceRooms ?? 0} />
            </CCol>
          </CRow>

          <CRow className="g-4 mb-4">
            <CCol md={12}>
              <CWidgetStatsF
                title="Occupancy rate"
                value={`${Number(occupancy.occupancyRate || 0).toFixed(2)}%`}
              />
            </CCol>
          </CRow>

          <BreakdownListCard
            title="Room status breakdown"
            items={occupancy.roomStatusBreakdown || []}
          />

          <CCard>
            <CCardHeader>Occupancy rows</CCardHeader>
            <CCardBody>
              <CTable responsive hover>
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>Room ID</CTableHeaderCell>
                    <CTableHeaderCell>Room number</CTableHeaderCell>
                    <CTableHeaderCell>Room type</CTableHeaderCell>
                    <CTableHeaderCell>Floor</CTableHeaderCell>
                    <CTableHeaderCell>Occupancy status</CTableHeaderCell>
                    <CTableHeaderCell>Housekeeping</CTableHeaderCell>
                    <CTableHeaderCell>Technical</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {(occupancy.rows || []).map((row) => (
                    <CTableRow key={row.roomId}>
                      <CTableDataCell>{row.roomId}</CTableDataCell>
                      <CTableDataCell>{row.roomNumber || '—'}</CTableDataCell>
                      <CTableDataCell>{row.roomType || '—'}</CTableDataCell>
                      <CTableDataCell>{row.floor ?? '—'}</CTableDataCell>
                      <CTableDataCell>{row.occupancyStatus || '—'}</CTableDataCell>
                      <CTableDataCell>{row.housekeepingStatus || '—'}</CTableDataCell>
                      <CTableDataCell>{row.technicalStatus || '—'}</CTableDataCell>
                    </CTableRow>
                  ))}

                  {!loading && (!occupancy.rows || occupancy.rows.length === 0) ? (
                    <CTableRow>
                      <CTableDataCell colSpan={7} className="text-center text-body-secondary py-4">
                        No occupancy rows found for selected period.
                      </CTableDataCell>
                    </CTableRow>
                  ) : null}
                </CTableBody>
              </CTable>
            </CCardBody>
          </CCard>
        </>
      ) : null}
    </>
  )
}

export default Reports