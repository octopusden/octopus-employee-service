package org.octopusden.employee.service.onec.client

import feign.RetryableException
import feign.Retryer
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

const val numberAttempts: Int = 5
const val timeDelayAttempt: Int = 300
const val numberIterations: Int = 5

class OneCRetry(private val timeRetryInMillis: Int = 60000) : Retryer {
    private val timeDelayIteration: Int = timeRetryInMillis / numberIterations - (numberAttempts * timeDelayAttempt)
    private val stopTime = System.currentTimeMillis() + timeRetryInMillis

    private var attempt: Int = numberAttempts
    private var iteration: Int = numberIterations

    companion object {
        private val logger = LoggerFactory.getLogger(OneCRetry::class.java)
    }

    override fun continueOrPropagate(e: RetryableException?) {
        if (stopTime < System.currentTimeMillis()) {
            throw e?.cause!!
        }

        logger.debug("Retry: iteration=${numberIterations - iteration + 1}, attempt=${numberAttempts - attempt + 1}")

        if (attempt-- > 0) {
            TimeUnit.MILLISECONDS.sleep(timeDelayAttempt.toLong())
        } else if (iteration-- > 0) {
            attempt = numberAttempts

            TimeUnit.MILLISECONDS.sleep(timeDelayIteration.toLong())
        } else {
            throw e?.cause!!
        }
    }

    override fun clone(): Retryer {
        return OneCRetry(timeRetryInMillis)
    }
}
