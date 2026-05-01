import React, { useEffect, useRef, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import {
  CContainer,
  CDropdown,
  CDropdownItem,
  CDropdownMenu,
  CDropdownToggle,
  CFormSelect,
  CHeader,
  CHeaderNav,
  CHeaderToggler,
  CNavLink,
  CNavItem,
  useColorModes,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilContrast, cilMenu, cilMoon, cilSun } from '@coreui/icons'
import { AppBreadcrumb } from './index'
import { AppHeaderDropdown } from './header/index'
import { useAuth } from '../context/AuthContext'
import { apiFetch } from '../services/api'

const AppHeader = () => {
  const headerRef = useRef()
  const { colorMode, setColorMode } = useColorModes('coreui-free-react-admin-template-theme')
  const dispatch = useDispatch()
  const sidebarShow = useSelector((state) => state.sidebarShow)

  const { token, selectedHotelId, setSelectedHotelId, setSelectedHotelName } = useAuth()

  const [hotels, setHotels] = useState([])
  const [loadingHotels, setLoadingHotels] = useState(false)

  useEffect(() => {
    const handleScroll = () => {
      headerRef.current?.classList.toggle('shadow-sm', document.documentElement.scrollTop > 0)
    }

    document.addEventListener('scroll', handleScroll)
    return () => document.removeEventListener('scroll', handleScroll)
  }, [])

  useEffect(() => {
    const loadHotels = async () => {
      try {
        setLoadingHotels(true)

        const data = await apiFetch('/api/hotels', {}, token)

        const activeHotels = Array.isArray(data)
          ? data.filter((hotel) => hotel.status !== 'INACTIVE')
          : []

        setHotels(activeHotels)

        if (activeHotels.length > 0) {
          const selectedExists = activeHotels.some(
            (hotel) => String(hotel.id) === String(selectedHotelId),
          )

          if (!selectedHotelId || !selectedExists) {
            setSelectedHotelId(String(activeHotels[0].id))
            setSelectedHotelName(activeHotels[0].name || '')
          }
        }
      } catch (error) {
        console.error('Failed to load hotels for header dropdown:', error)
        setHotels([])
      } finally {
        setLoadingHotels(false)
      }
    }

    if (token) {
      loadHotels()
    }
  }, [token])

  return (
    <CHeader position="sticky" className="mb-4 p-0" ref={headerRef}>
      <CContainer className="border-bottom px-4" fluid>
        <CHeaderToggler
          onClick={() => dispatch({ type: 'set', sidebarShow: !sidebarShow })}
          style={{ marginInlineStart: '-14px' }}
        >
          <CIcon icon={cilMenu} size="lg" />
        </CHeaderToggler>

        <CHeaderNav className="d-none d-md-flex me-3">
          <CNavItem>
            <CNavLink to="/dashboard" as={NavLink}>
              Dashboard
            </CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink to="/hotels" as={NavLink}>
              Hotels
            </CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink to="/rooms" as={NavLink}>
              Rooms
            </CNavLink>
          </CNavItem>
          <CNavItem>
            <CNavLink to="/bookings" as={NavLink}>
              Bookings
            </CNavLink>
          </CNavItem>
        </CHeaderNav>

        <div className="ms-auto me-3" style={{ minWidth: 240 }}>
          <CFormSelect
            value={selectedHotelId || ''}
            onChange={(e) => {
              const hotelId = e.target.value
              const selectedHotel = hotels.find((hotel) => String(hotel.id) === String(hotelId))

              setSelectedHotelId(hotelId)
              setSelectedHotelName(selectedHotel?.name || '')
            }}
            disabled={loadingHotels || hotels.length === 0}
          >
            {hotels.length === 0 ? (
              <option value="">No hotels</option>
            ) : (
              hotels.map((hotel) => (
                <option key={hotel.id} value={hotel.id}>
                  {hotel.name}
                </option>
              ))
            )}
          </CFormSelect>
        </div>

        <CHeaderNav>
          <li className="nav-item py-1">
            <div className="vr h-100 mx-2 text-body text-opacity-75"></div>
          </li>

          <CDropdown variant="nav-item" placement="bottom-end">
            <CDropdownToggle caret={false}>
              {colorMode === 'dark' ? (
                <CIcon icon={cilMoon} size="lg" />
              ) : colorMode === 'auto' ? (
                <CIcon icon={cilContrast} size="lg" />
              ) : (
                <CIcon icon={cilSun} size="lg" />
              )}
            </CDropdownToggle>
            <CDropdownMenu>
              <CDropdownItem
                active={colorMode === 'light'}
                as="button"
                type="button"
                onClick={() => setColorMode('light')}
              >
                <CIcon className="me-2" icon={cilSun} size="lg" />
                Light
              </CDropdownItem>

              <CDropdownItem
                active={colorMode === 'dark'}
                as="button"
                type="button"
                onClick={() => setColorMode('dark')}
              >
                <CIcon className="me-2" icon={cilMoon} size="lg" />
                Dark
              </CDropdownItem>

              <CDropdownItem
                active={colorMode === 'auto'}
                as="button"
                type="button"
                onClick={() => setColorMode('auto')}
              >
                <CIcon className="me-2" icon={cilContrast} size="lg" />
                Auto
              </CDropdownItem>
            </CDropdownMenu>
          </CDropdown>

          <li className="nav-item py-1">
            <div className="vr h-100 mx-2 text-body text-opacity-75"></div>
          </li>

          <AppHeaderDropdown />
        </CHeaderNav>
      </CContainer>

      <CContainer className="px-4" fluid>
        <AppBreadcrumb />
      </CContainer>
    </CHeader>
  )
}

export default AppHeader