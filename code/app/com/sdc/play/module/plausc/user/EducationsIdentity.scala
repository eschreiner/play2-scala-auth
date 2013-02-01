package com.sdc.play.module.plausc.user

import java.util.Collection;

trait EducationsIdentity {

    def getEducations(): Seq[EducationInfo]

}

case class EducationInfo(id: String,
		schoolName: String, degree: String,
		startDateYear: Int, endDateYear: Int)

import org.apache.commons.lang3.StringUtils

object EducationInfo {

    import org.codehaus.jackson.JsonNode
    import com.sdc.play.module.plausc.providers.util.JsonHelpers._
    import Education._

	def apply(node: JsonNode): EducationInfo = {
		val id = asText(node, ID)
		val schoolName = asText(node, SCHOOL_NAME)
		val degree     = asText(node, DEGREE)

		val startDateYear = asInt(node, START_DATE_YEAR)
		val endDateYear   = asInt(node, END_DATE_YEAR)

		EducationInfo(id, schoolName, degree, startDateYear, endDateYear)
	}

	private object Education {
		val ID              = "id"
		val SCHOOL_NAME     = "schoolName"
		val DEGREE          = "degree"
		val START_DATE_YEAR = "startDate/year"
		val END_DATE_YEAR   = "endDate/year"
	}

}
