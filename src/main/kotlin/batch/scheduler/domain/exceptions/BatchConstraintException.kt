package batch.scheduler.domain.exceptions


class BatchConstraintException : BusinessException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
    constructor(ex: Exception): super(ex)
}