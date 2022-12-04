package `in`.silive.lateentryproject.models

import `in`.silive.lateentryproject.entities.Student

data class BulkDataClass(
    val student_data: List<Student>,
    val venue_data: List<VenueData>
)