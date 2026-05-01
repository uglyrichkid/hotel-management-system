import React, { useEffect, useMemo, useState } from 'react'
import {
  CAlert,
  CBadge,
  CCard,
  CCardBody,
  CCardHeader,
  CCol,
  CProgress,
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
import {
  cilBed,
  cilCalendar,
  cilDollar,
  cilInfo,
  cilRoom,
} from '@coreui/icons'
import {
  BarChart,
  Bar,
  CartesianGrid,
  Cell,
  Legend,
  LineChart,
  Line,
  PieChart,
  Pie,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { useAuth } from '../../context/AuthContext'
import { reportsApi } from '../../services/api'
import SelectedHotelAlert from '../../components/common/SelectedHotelAlert'
import PageHeader from '../../components/common/PageHeader'

const formatMoney = (value) => {
  const num = Number(value || 0)
  return num.toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

const formatMoneyWithCurrency = (value, currencyCode) => {
  const formatted = formatMoney(value)
  return currencyCode ? `${formatted} ${currencyCode}` : formatted
}

const getDefaultDateRange = () => {
  const today = new Date()
  const to = today.toISOString().slice(0, 10)
  const fromDate = new Date(today)
  fromDate.setDate(today.getDate() - 29)
  const from = fromDate.toISOString().slice(0, 10)
  return { from, to }
}

const getBadgeColor = (label) => {
  switch ((label || '').toUpperCase()) {
    case 'CONFIRMED':
    case 'COMPLETED':
    case 'AVAILABLE':
    case 'ACTIVE':
      return 'success'
    case 'CHECKED_IN':
    case 'PARTIALLY_PAID':
    case 'OCCUPIED':
      return 'warning'
    case 'CANCELLED':
    case 'FAILED':
    case 'OUT_OF_SERVICE':
      return 'danger'
    case 'CREATED':
    case 'PENDING':
      return 'info'
    default:
      return 'secondary'
  }
}

const chartColors = [
  '#321fdb',
  '#2eb85c',
  '#f9b115',
  '#e55353',
  '#39f',
  '#8a93a2',
  '#d63384',
  '#20c997',
]

const Dashboard = () => {
  const { token, selectedHotelId, user } = useAuth()
  const defaultRange = useMemo(() => getDefaultDateRange(), [])
  const [state, setState] = useState({
    loading: true,
    error: '',
    data: null,
  })

  useEffect(() => {
    if (!selectedHotelId) {
      setState({ loading: false, error: '', data: null })
      return
    }

    const load = async () => {
      try {
        setState((prev) => ({ ...prev, loading: true, error: '' }))

        const params = {
          hotelId: selectedHotelId,
          dateFrom: defaultRange.from,
          dateTo: defaultRange.to,
        }

        const [bookingReport, revenueReport, occupancyReport] = await Promise.all([
          reportsApi.getBookings(params, token),
          reportsApi.getRevenue(params, token),
          reportsApi.getOccupancy(params, token),
        ])

        setState({
          loading: false,
          error: '',
          data: { bookingReport, revenueReport, occupancyReport },
        })
      } catch (error) {
        setState({
          loading: false,
          error: error.message || 'Failed to load dashboard',
          data: null,
        })
      }
    }

    load()
  }, [selectedHotelId, token, defaultRange])

  if (!selectedHotelId) {
    return (
      <>
        <PageHeader
          title="Dashboard"
          subtitle="Quick overview of hotel operations, occupancy, and revenue."
        />
        <SelectedHotelAlert />
      </>
    )
  }

  const data = state.data
  const bookingReport = data?.bookingReport
  const revenueReport = data?.revenueReport
  const occupancyReport = data?.occupancyReport

  const dashboardCurrency =
    revenueReport?.currencyCode ||
    bookingReport?.currencyCode ||
    ''

  const occupancyRate = Number(occupancyReport?.occupancyRate || 0)

  const bookingTrendChart = (bookingReport?.dailyTrend || []).map((item) => ({
    date: item.date?.slice(5) || item.date,
    value: Number(item.value || 0),
  }))

  const revenueTrendChart = (revenueReport?.revenueByDay || []).map((item) => ({
    date: item.date?.slice(5) || item.date,
    value: Number(item.value || 0),
  }))

  const occupancyTrendChart = (occupancyReport?.occupancyByDay || []).map((item) => ({
    date: item.date?.slice(5) || item.date,
    value: Number(item.value || 0),
  }))

  const bookingStatusChart = (bookingReport?.statusBreakdown || []).map((item) => ({
    name: item.label,
    value: Number(item.count || 0),
  }))

  const paymentMethodChart = (revenueReport?.paymentMethodBreakdown || []).map((item) => ({
    name: item.label,
    value: Number(item.amount || 0),
  }))

  const roomStatusChart = (occupancyReport?.roomStatusBreakdown || []).map((item) => ({
    name: item.label,
    value: Number(item.count || 0),
  }))

  const attentionItems = [
    {
      label: 'Outstanding balance',
      value: formatMoneyWithCurrency(revenueReport?.outstandingBalance, dashboardCurrency),
      show: Number(revenueReport?.outstandingBalance || 0) > 0,
      color: 'warning',
    },
    {
      label: 'Failed payments',
      value: revenueReport?.failedPaymentsCount ?? 0,
      show: Number(revenueReport?.failedPaymentsCount || 0) > 0,
      color: 'danger',
    },
    {
      label: 'Unpaid bookings',
      value: revenueReport?.unpaidBookingsCount ?? 0,
      show: Number(revenueReport?.unpaidBookingsCount || 0) > 0,
      color: 'warning',
    },
    {
      label: 'Out of service rooms',
      value: occupancyReport?.outOfServiceRooms ?? 0,
      show: Number(occupancyReport?.outOfServiceRooms || 0) > 0,
      color: 'danger',
    },
  ].filter((item) => item.show)

  return (
    <>
      <PageHeader
        title="Dashboard"
        subtitle={`Welcome${user?.fullName ? `, ${user.fullName}` : ''}. Overview for hotel ID ${selectedHotelId}.`}
      />

      {state.error ? <CAlert color="danger">{state.error}</CAlert> : null}
      {state.loading ? <CSpinner color="primary" /> : null}

      {data ? (
        <>
          <CRow>
            <CCol sm={6} xl={3}>
              <CWidgetStatsF
                className="mb-4"
                color="primary"
                icon={<CIcon icon={cilCalendar} height={24} />}
                title="Total Bookings"
                value={bookingReport?.totalBookings ?? 0}
              />
            </CCol>
            <CCol sm={6} xl={3}>
              <CWidgetStatsF
                className="mb-4"
                color="success"
                icon={<CIcon icon={cilDollar} height={24} />}
                title="Collected Revenue"
                value={formatMoneyWithCurrency(revenueReport?.collectedRevenue, dashboardCurrency)}
              />
            </CCol>
            <CCol sm={6} xl={3}>
              <CWidgetStatsF
                className="mb-4"
                color="warning"
                icon={<CIcon icon={cilBed} height={24} />}
                title="Occupied Rooms"
                value={occupancyReport?.occupiedRooms ?? 0}
              />
            </CCol>
            <CCol sm={6} xl={3}>
              <CWidgetStatsF
                className="mb-4"
                color="info"
                icon={<CIcon icon={cilRoom} height={24} />}
                title="Available Rooms"
                value={occupancyReport?.availableRooms ?? 0}
              />
            </CCol>
          </CRow>

          <CRow>
            <CCol lg={4}>
              <CCard className="mb-4 h-100">
                <CCardHeader>Revenue snapshot</CCardHeader>
                <CCardBody>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Expected revenue</span>
                    <strong>
                      {formatMoneyWithCurrency(revenueReport?.expectedRevenue, dashboardCurrency)}
                    </strong>
                  </div>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Collected revenue</span>
                    <strong>
                      {formatMoneyWithCurrency(revenueReport?.collectedRevenue, dashboardCurrency)}
                    </strong>
                  </div>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Outstanding balance</span>
                    <strong>
                      {formatMoneyWithCurrency(revenueReport?.outstandingBalance, dashboardCurrency)}
                    </strong>
                  </div>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Partial bookings</span>
                    <CBadge color="warning">{revenueReport?.partialBookingsCount ?? 0}</CBadge>
                  </div>
                  <div className="d-flex justify-content-between">
                    <span>Failed payments</span>
                    <CBadge color="danger">{revenueReport?.failedPaymentsCount ?? 0}</CBadge>
                  </div>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol lg={4}>
              <CCard className="mb-4 h-100">
                <CCardHeader>Occupancy snapshot</CCardHeader>
                <CCardBody>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Total rooms</span>
                    <strong>{occupancyReport?.totalRooms ?? 0}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Occupied rooms</span>
                    <strong>{occupancyReport?.occupiedRooms ?? 0}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-3">
                    <span>Available rooms</span>
                    <strong>{occupancyReport?.availableRooms ?? 0}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>Out of service</span>
                    <CBadge color="danger">{occupancyReport?.outOfServiceRooms ?? 0}</CBadge>
                  </div>
                  <div className="mt-4">
                    <div className="d-flex justify-content-between mb-2">
                      <span>Occupancy rate</span>
                      <strong>{occupancyRate}%</strong>
                    </div>
                    <CProgress value={occupancyRate} />
                  </div>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol lg={4}>
              <CCard className="mb-4 h-100">
                <CCardHeader>Attention needed</CCardHeader>
                <CCardBody>
                  {attentionItems.length ? (
                    attentionItems.map((item) => (
                      <div
                        key={item.label}
                        className="d-flex justify-content-between align-items-center py-2 border-bottom"
                      >
                        <span>{item.label}</span>
                        <CBadge color={item.color}>{item.value}</CBadge>
                      </div>
                    ))
                  ) : (
                    <div className="text-success">No critical operational issues for this period.</div>
                  )}
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow>
            <CCol xl={8}>
              <CCard className="mb-4">
                <CCardHeader>Revenue trend</CCardHeader>
                <CCardBody style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={revenueTrendChart}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis />
                      <Tooltip formatter={(value) => formatMoneyWithCurrency(value, dashboardCurrency)} />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="value"
                        name="Revenue"
                        stroke="#2eb85c"
                        strokeWidth={3}
                        dot={false}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol xl={4}>
              <CCard className="mb-4">
                <CCardHeader>Payment methods</CCardHeader>
                <CCardBody style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={paymentMethodChart}
                        dataKey="value"
                        nameKey="name"
                        outerRadius={100}
                        label
                      >
                        {paymentMethodChart.map((entry, index) => (
                          <Cell
                            key={`cell-${entry.name}`}
                            fill={chartColors[index % chartColors.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => formatMoneyWithCurrency(value, dashboardCurrency)} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow>
            <CCol xl={6}>
              <CCard className="mb-4">
                <CCardHeader>Booking trend</CCardHeader>
                <CCardBody style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={bookingTrendChart}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis allowDecimals={false} />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="value" name="Bookings" fill="#321fdb" radius={[6, 6, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol xl={6}>
              <CCard className="mb-4">
                <CCardHeader>Occupancy trend</CCardHeader>
                <CCardBody style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={occupancyTrendChart}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis />
                      <Tooltip formatter={(value) => `${value}%`} />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="value"
                        name="Occupancy %"
                        stroke="#f9b115"
                        strokeWidth={3}
                        dot={false}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow>
            <CCol lg={6}>
              <CCard className="mb-4">
                <CCardHeader>Booking status distribution</CCardHeader>
                <CCardBody style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={bookingStatusChart}
                        dataKey="value"
                        nameKey="name"
                        outerRadius={100}
                        label
                      >
                        {bookingStatusChart.map((entry, index) => (
                          <Cell
                            key={`cell-${entry.name}`}
                            fill={chartColors[index % chartColors.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol lg={6}>
              <CCard className="mb-4">
                <CCardHeader>Room status distribution</CCardHeader>
                <CCardBody style={{ height: 320 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={roomStatusChart}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis allowDecimals={false} />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="value" name="Rooms" radius={[6, 6, 0, 0]}>
                        {roomStatusChart.map((entry, index) => (
                          <Cell
                            key={`cell-${entry.name}`}
                            fill={chartColors[index % chartColors.length]}
                          />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow>
            <CCol lg={7}>
              <CCard className="mb-4">
                <CCardHeader>Recent booking rows</CCardHeader>
                <CCardBody>
                  <CTable responsive hover align="middle">
                    <CTableHead>
                      <CTableRow>
                        <CTableHeaderCell>ID</CTableHeaderCell>
                        <CTableHeaderCell>Guest</CTableHeaderCell>
                        <CTableHeaderCell>Rooms</CTableHeaderCell>
                        <CTableHeaderCell>Status</CTableHeaderCell>
                        <CTableHeaderCell>Total</CTableHeaderCell>
                        <CTableHeaderCell>Remaining</CTableHeaderCell>
                      </CTableRow>
                    </CTableHead>
                    <CTableBody>
                      {(bookingReport?.rows || []).slice(0, 8).length ? (
                        bookingReport.rows.slice(0, 8).map((row) => {
                          const rowCurrency = row.currencyCode || bookingReport?.currencyCode || dashboardCurrency
                          return (
                            <CTableRow key={row.bookingId}>
                              <CTableDataCell>{row.bookingId}</CTableDataCell>
                              <CTableDataCell>{row.guestName || '—'}</CTableDataCell>
                              <CTableDataCell>{row.rooms || '—'}</CTableDataCell>
                              <CTableDataCell>
                                <CBadge color={getBadgeColor(row.status)}>{row.status || '—'}</CBadge>
                              </CTableDataCell>
                              <CTableDataCell>
                                {formatMoneyWithCurrency(row.totalAmount, rowCurrency)}
                              </CTableDataCell>
                              <CTableDataCell>
                                {formatMoneyWithCurrency(row.remainingAmount, rowCurrency)}
                              </CTableDataCell>
                            </CTableRow>
                          )
                        })
                      ) : (
                        <CTableRow>
                          <CTableDataCell colSpan={6} className="text-center text-medium-emphasis">
                            No booking data found.
                          </CTableDataCell>
                        </CTableRow>
                      )}
                    </CTableBody>
                  </CTable>
                </CCardBody>
              </CCard>
            </CCol>

            <CCol lg={5}>
              <CCard className="mb-4">
                <CCardHeader>Room operational view</CCardHeader>
                <CCardBody>
                  <CTable responsive hover align="middle">
                    <CTableHead>
                      <CTableRow>
                        <CTableHeaderCell>Room</CTableHeaderCell>
                        <CTableHeaderCell>Occupancy</CTableHeaderCell>
                        <CTableHeaderCell>Housekeeping</CTableHeaderCell>
                        <CTableHeaderCell>Technical</CTableHeaderCell>
                      </CTableRow>
                    </CTableHead>
                    <CTableBody>
                      {(occupancyReport?.rows || []).slice(0, 8).length ? (
                        occupancyReport.rows.slice(0, 8).map((row) => (
                          <CTableRow key={row.roomId}>
                            <CTableDataCell>{row.roomNumber || '—'}</CTableDataCell>
                            <CTableDataCell>
                              <CBadge color={getBadgeColor(row.occupancyStatus)}>
                                {row.occupancyStatus || '—'}
                              </CBadge>
                            </CTableDataCell>
                            <CTableDataCell>{row.housekeepingStatus || '—'}</CTableDataCell>
                            <CTableDataCell>
                              <CBadge color={getBadgeColor(row.technicalStatus)}>
                                {row.technicalStatus || '—'}
                              </CBadge>
                            </CTableDataCell>
                          </CTableRow>
                        ))
                      ) : (
                        <CTableRow>
                          <CTableDataCell colSpan={4} className="text-center text-medium-emphasis">
                            No room data found.
                          </CTableDataCell>
                        </CTableRow>
                      )}
                    </CTableBody>
                  </CTable>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>

          <CRow>
            <CCol xs={12}>
              <CCard className="mb-4">
                <CCardBody className="d-flex align-items-center gap-2">
                  <CIcon icon={cilInfo} />
                  <span className="text-medium-emphasis">
                    This dashboard is based on the selected hotel and the last 30 days of reporting data.
                  </span>
                </CCardBody>
              </CCard>
            </CCol>
          </CRow>
        </>
      ) : null}
    </>
  )
}

export default Dashboard