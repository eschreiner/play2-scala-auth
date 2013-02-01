package com.sdc.play.module.plausc.user

trait EmploymentsIdentity {

	def getEmployments(): Seq[EmploymentInfo]

}

case class EmploymentInfo(id: String,
		title: String, summary: String,
		startDateMonth: Int, startDateYear: Int,
		endDateMonth: Int, endDateYear: Int,
		isCurrent: Boolean, companyName: String)

object EmploymentInfo {

    import org.codehaus.jackson.JsonNode
    import com.sdc.play.module.plausc.providers.util.JsonHelpers._
    import Employment._

    def apply(node: JsonNode): EmploymentInfo = {

		val id      = asText(node, ID)
		val title   = asText(node, TITLE)
		val summary = asText(node, SUMMARY)

		val startDateMonth = asInt(node, START_DATE_MONTH)
		val startDateYear  = asInt(node, START_DATE_YEAR)
		val endDateMonth   = asInt(node, END_DATE_MONTH)
		val endDateYear    = asInt(node, END_DATE_YEAR)

		val isCurrent      = asBool(node, IS_CURRENT)
		val companyName    = asText(node, COMPANY_NAME)

		EmploymentInfo(id, title, summary, startDateMonth, startDateYear,
		        endDateMonth, endDateYear, isCurrent, companyName)
	}

   	private object Employment {
		val ID               = "id"
		val TITLE            = "title"
		val SUMMARY          = "summary"
		val START_DATE_MONTH = "startDate/month"
		val START_DATE_YEAR  = "startDate/year"
		val END_DATE_MONTH   = "endDate/month"
		val END_DATE_YEAR    = "endDate/year"
		val IS_CURRENT       = "isCurrent"
		val COMPANY_NAME     = "company/name"
	}

}
