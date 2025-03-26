package id.nearyou.app.repository

import id.nearyou.app.domain.Location
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LocationRepository : JpaRepository<Location, UUID> {
    
    @Query("""
        SELECT l FROM Location l 
        WHERE function('ST_DistanceSphere', 
            function('ST_MakePoint', l.longitude, l.latitude),
            function('ST_MakePoint', :longitude, :latitude)
        ) <= :radius
    """)
    fun findLocationsWithinRadius(latitude: Double, longitude: Double, radius: Double): List<Location>
    
    @Query("""
        SELECT DISTINCT l.city FROM Location l 
        WHERE l.city IS NOT NULL
        ORDER BY l.city
    """)
    fun findAllCities(): List<String>
    
    @Query("""
        SELECT DISTINCT l.country FROM Location l 
        WHERE l.country IS NOT NULL
        ORDER BY l.country
    """)
    fun findAllCountries(): List<String>
}
