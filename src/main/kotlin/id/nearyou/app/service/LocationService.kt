package id.nearyou.app.service

import id.nearyou.app.domain.Location
import id.nearyou.app.repository.LocationRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LocationService(
    private val locationRepository: LocationRepository
) {

    fun findById(id: UUID): Location? {
        return locationRepository.findById(id).orElse(null)
    }
    
    fun findNearbyLocations(latitude: Double, longitude: Double, radius: Double = 5000.0): List<Location> {
        return locationRepository.findLocationsWithinRadius(latitude, longitude, radius)
    }
    
    fun getAllCities(): List<String> {
        return locationRepository.findAllCities()
    }
    
    fun getAllCountries(): List<String> {
        return locationRepository.findAllCountries()
    }
    
    fun save(location: Location): Location {
        return locationRepository.save(location)
    }
    
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Haversine formula to calculate distance between two points on Earth
        val r = 6371e3 // Earth's radius in meters
        
        val phi1 = lat1 * Math.PI / 180
        val phi2 = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (lon2 - lon1) * Math.PI / 180
        
        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return r * c // Distance in meters
    }
}
