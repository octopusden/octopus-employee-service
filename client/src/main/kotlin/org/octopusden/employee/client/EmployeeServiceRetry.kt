package org.octopusden.employee.client

import feign.RetryableException
import feign.Retryer
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

const val numberAttempts: Int = 5
const val timeDelayAttempt: Int = 300
const val numberIterations: Int = 5

class EmployeeServiceRetry(private val timeRetryInMillis: Int = 60000) : Retryer {
    private val timeDelayIteration: Int = timeRetryInMillis / numberIterations - (numberAttempts * timeDelayAttempt)
    private val stopTime = System.currentTimeMillis() + timeRetryInMillis

    private var attempt: Int = numberAttempts
    private var iteration: Int = numberIterations

    companion object {
        private val logger = LoggerFactory.getLogger(EmployeeServiceRetry::class.java)
    }

    override fun continueOrPropagate(e: RetryableException?) {
        if (stopTime < System.currentTimeMillis()) {
            throw e?.cause!!
        }

        logger.debug("Retry: iteration=${numberIterations - iteration + 1}, attempt=${numberAttempts - attempt + 1}")

        when {
            attempt-- > 0 -> {
                TimeUnit.MILLISECONDS.sleep(timeDelayAttempt.toLong())
            }
            iteration-- > 0 -> {
                attempt = numberAttempts
                TimeUnit.MILLISECONDS.sleep(timeDelayIteration.toLong())
            }
            else -> {
                throw e?.cause!!
            }
        }
    }

    override fun clone(): Retryer {
        return EmployeeServiceRetry(timeRetryInMillis)
    }
}
