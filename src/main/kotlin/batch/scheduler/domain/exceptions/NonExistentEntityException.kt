package batch.scheduler.domain.exceptions


class NonExistentEntityException : BusinessException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
    constructor(ex: Exception): super(ex)
}