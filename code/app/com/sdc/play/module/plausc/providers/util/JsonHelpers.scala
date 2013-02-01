package com.sdc.play.module.plausc.providers.util

/**
 * @author  Dr. Erich W. Schreiner - Software Design &amp; Consulting GmbH
 * @version 0.1.0.0
 * @since   0.1.0.0
 */
object JsonHelpers {

    import org.codehaus.jackson.JsonNode

	/**
	 * As text.
	 * @param node the root node
	 * @param expr the expression for traversing the node tree
	 * @return the content of the specified node or <code>null</code>
	 */
	def asText(node: JsonNode, expr: String): String = asText(find(node,expr))
	def asText(node: Option[JsonNode]): String = asText(node orNull)
	def asText(node: JsonNode): String = if (node != null) node.asText() else null

	/**
	 * As int.
	 * @param node the root node
	 * @param expr the expression for traversing the node tree
	 * @return the content of the specified node or 0
	 */
	def asInt(node: JsonNode, expr: String): Int = asInt(find(node,expr))
	def asInt(node: Option[JsonNode]): Int = asInt(node orNull)
	def asInt(node: JsonNode): Int = if (node != null) node.asInt() else 0

	/**
	 * As long.
	 * @param node the root node
	 * @param expr the expression for traversing the node tree
	 * @return the content of the specified node or 0
	 */
	def asLong(node: JsonNode, expr: String): Long = asLong(find(node,expr))
	def asLong(node: Option[JsonNode]): Long = asLong(node orNull)
	def asLong(node: JsonNode): Long = if (node != null) node.asLong() else 0

	/**
	 * As bool.
	 * @param node the root node
	 * @param expr the expression for traversing the node tree
	 * @return the content of the specified node or false
	 */
	def asBool(node: JsonNode, expr: String): Boolean = asBool(find(node,expr))
	def asBool(node: Option[JsonNode]): Boolean = asBool(node orNull)
	def asBool(node: JsonNode): Boolean = if (node != null) node.asBoolean() else false

	def find(node: JsonNode, childExpression: String): Option[JsonNode] = {
		var current = node
		for (segment <- childExpression.split("/")) {
			if (current != null) {
				current = current.get(segment)
			} else {
				return None
			}
		}
		Option(current)
	}

}
