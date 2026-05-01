import React, { Suspense } from 'react'
import { Navigate, Route, Routes, useParams } from 'react-router-dom'
import { CContainer, CSpinner } from '@coreui/react'
import routes from '../routes'
import { useAuth } from '../context/AuthContext'

const ProtectedRouteElement = ({ route }) => {
  const { canAccessRoute, canAccessHotel, selectedHotelId, isSystemAdmin } = useAuth()
  const params = useParams()

  if (!canAccessRoute(route.roles || [])) {
    return <Navigate to="/dashboard" replace />
  }

  if (route.requiresHotelAccess) {
    const routeHotelId = params.id

    if (!isSystemAdmin && !canAccessHotel(routeHotelId || selectedHotelId)) {
      return <Navigate to="/dashboard" replace />
    }
  }

  return <route.element />
}

const MyHotelRedirect = () => {
  const { selectedHotelId, isHotelAdmin } = useAuth()

  if (!isHotelAdmin || !selectedHotelId) {
    return <Navigate to="/dashboard" replace />
  }

  return <Navigate to={`/hotels/${selectedHotelId}/manage`} replace />
}

const AppContent = () => {
  return (
    <CContainer className="px-4" lg>
      <Suspense fallback={<CSpinner color="primary" />}>
        <Routes>
          <Route path="/my-hotel" element={<MyHotelRedirect />} />

          {routes.map((route, idx) =>
            route.element ? (
              <Route
                key={idx}
                path={route.path}
                element={<ProtectedRouteElement route={route} />}
              />
            ) : null,
          )}

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Suspense>
    </CContainer>
  )
}

export default React.memo(AppContent)