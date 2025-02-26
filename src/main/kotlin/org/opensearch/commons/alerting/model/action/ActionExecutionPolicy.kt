package org.opensearch.commons.alerting.model.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.notifications.model.BaseModel
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import java.io.IOException

data class ActionExecutionPolicy(
    val actionExecutionScope: ActionExecutionScope
) : BaseModel {

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this (
        ActionExecutionScope.readFrom(sin) // actionExecutionScope
    )

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        builder.startObject()
            .field(ACTION_EXECUTION_SCOPE, actionExecutionScope)
        return builder.endObject()
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        if (actionExecutionScope is PerAlertActionScope) {
            out.writeEnum(ActionExecutionScope.Type.PER_ALERT)
        } else {
            out.writeEnum(ActionExecutionScope.Type.PER_EXECUTION)
        }
        actionExecutionScope.writeTo(out)
    }

    companion object {
        const val ACTION_EXECUTION_SCOPE = "action_execution_scope"

        @JvmStatic
        @Throws(IOException::class)
        fun parse(xcp: XContentParser): ActionExecutionPolicy {
            lateinit var actionExecutionScope: ActionExecutionScope

            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.currentToken(), xcp)
            while (xcp.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = xcp.currentName()
                xcp.nextToken()

                when (fieldName) {
                    ACTION_EXECUTION_SCOPE -> actionExecutionScope = ActionExecutionScope.parse(xcp)
                }
            }

            return ActionExecutionPolicy(
                requireNotNull(actionExecutionScope) { "Action execution scope is null" }
            )
        }

        @JvmStatic
        @Throws(IOException::class)
        fun readFrom(sin: StreamInput): ActionExecutionPolicy {
            return ActionExecutionPolicy(sin)
        }

        /**
         * The default [ActionExecutionPolicy] configuration for Bucket-Level Monitors.
         *
         * If Query-Level Monitors integrate the use of [ActionExecutionPolicy] then a separate default configuration
         * will need to be made depending on the desired behavior.
         */
        fun getDefaultConfigurationForBucketLevelMonitor(): ActionExecutionPolicy {
            val defaultActionExecutionScope = PerAlertActionScope(
                actionableAlerts = setOf(AlertCategory.DEDUPED, AlertCategory.NEW)
            )
            return ActionExecutionPolicy(actionExecutionScope = defaultActionExecutionScope)
        }

        /**
         * The default [ActionExecutionPolicy] configuration for Document-Level Monitors.
         *
         * If Query-Level Monitors integrate the use of [ActionExecutionPolicy] then a separate default configuration
         * will need to be made depending on the desired behavior.
         */
        fun getDefaultConfigurationForDocumentLevelMonitor(): ActionExecutionPolicy {
            val defaultActionExecutionScope = PerAlertActionScope(
                actionableAlerts = setOf(AlertCategory.DEDUPED, AlertCategory.NEW)
            )
            return ActionExecutionPolicy(actionExecutionScope = defaultActionExecutionScope)
        }
    }
}
