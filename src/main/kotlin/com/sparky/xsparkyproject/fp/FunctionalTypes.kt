package com.sparky.xsparkyproject.fp

/**
 * функціональні типи даних та утиліти
 * включає option, either та інші функціональні концепції
 *
 * @author Андрій Будильников
 */
sealed class Option<out T> {
    companion object {
        fun <T> some(value: T): Option<T> = Some(value)
        fun <T> none(): Option<T> = None
        fun <T> fromNullable(value: T?): Option<T> = if (value == null) None else Some(value)
    }
}

data class Some<out T>(val value: T) : Option<T>()
object None : Option<Nothing>()

/**
 * результат операції який може бути або успіхом або помилкою
 */
sealed class Either<out L, out R> {
    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }
}

data class Left<out L>(val value: L) : Either<L, Nothing>()
data class Right<out R>(val value: R) : Either<Nothing, R>()

/**
 * тип для представлення результату операції з можливістю помилки
 */
sealed class Result<out T> {
    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun <T> failure(error: Throwable): Result<T> = Failure(error)
    }
}

data class Success<out T>(val value: T) : Result<T>()
data class Failure<out T>(val error: Throwable) : Result<T>()

/**
 * функції розширення для option
 */
fun <T, R> Option<T>.map(f: (T) -> R): Option<R> = when (this) {
    is Some -> Option.some(f(this.value))
    is None -> Option.none()
}

fun <T> Option<T>.getOrElse(default: () -> T): T = when (this) {
    is Some -> this.value
    is None -> default()
}

fun <T> Option<T>.orElse(alternative: () -> Option<T>): Option<T> = when (this) {
    is Some -> this
    is None -> alternative()
}

fun <T> Option<T>.filter(predicate: (T) -> Boolean): Option<T> = when (this) {
    is Some -> if (predicate(this.value)) this else Option.none()
    is None -> this
}

/**
 * функції розширення для either
 */
fun <L, R, S> Either<L, R>.map(f: (R) -> S): Either<L, S> = when (this) {
    is Left -> this
    is Right -> Either.right(f(this.value))
}

fun <L, R, S> Either<L, R>.flatMap(f: (R) -> Either<L, S>): Either<L, S> = when (this) {
    is Left -> this
    is Right -> f(this.value)
}

fun <L, R> Either<L, R>.fold(ifLeft: (L) -> R, ifRight: (R) -> R): R = when (this) {
    is Left -> ifLeft(this.value)
    is Right -> ifRight(this.value)
}

/**
 * функції розширення для result
 */
fun <T, R> Result<T>.map(f: (T) -> R): Result<R> = when (this) {
    is Success -> Result.success(f(this.value))
    is Failure -> Result.failure(this.error)
}

fun <T> Result<T>.fold(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Unit = when (this) {
    is Success -> onSuccess(this.value)
    is Failure -> onFailure(this.error)
}

fun <T> Result<T>.getOrElse(default: (Throwable) -> T): T = when (this) {
    is Success -> this.value
    is Failure -> default(this.error)
}