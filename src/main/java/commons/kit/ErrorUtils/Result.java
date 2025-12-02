package commons.kit.ErrorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A container representing either a successful result (Success) or a failure (Failure).
 * Follows the Railway Oriented Programming pattern for explicit error handling.
 *
 * @param <E> the error type
 * @param <V> the value type
 */
public abstract class Result<E, V> {

    private Result() {}

    // ========================================================================
    // FACTORY METHODS
    // ========================================================================

    /**
     * Creates a successful Result containing the given value.
     */
    public static <E, V> Result<E, V> ok(V value) {
        return new Success<>(value);
    }

    /**
     * Creates a failed Result containing the given error.
     * Error must not be null.
     */
    public static <E, V> Result<E, V> err(E error) {
        if (error == null) {
            throw new IllegalArgumentException("Error must not be null");
        }
        return new Failure<>(error);
    }

    /**
     * Executes a potentially throwing operation and wraps the result.
     */
    public static <V> Result<Throwable, V> of(CheckedSupplier<V> supplier) {
        try {
            return ok(supplier.get());
        } catch (Throwable t) {
            return err(t);
        }
    }

    /**
     * Executes a potentially throwing operation with custom error mapping.
     */
    public static <E, V> Result<E, V> of(CheckedSupplier<V> supplier, Fn<Throwable, E> mapper) {
        try {
            return ok(supplier.get());
        } catch (Throwable t) {
            return err(mapper.apply(t));
        }
    }

    /**
     * Converts a list of Results into a Result of list.
     * If any Result is a Failure, returns the first Failure encountered.
     */
    public static <E, V> Result<E, List<V>> sequence(List<Result<E, V>> results) {
        List<V> values = new ArrayList<>();
        for (Result<E, V> result : results) {
            if (result.isErr()) {
                return err(result.getErrOrElse(null));
            }
            values.add(result.getOrElse(null));
        }
        return ok(values);
    }

    /**
     * Flattens a nested Result.
     */
    public static <E, V> Result<E, V> flatten(Result<E, Result<E, V>> nested) {
        return nested.flatMap(inner -> inner);
    }

    /**
     * Creates a Result from a CompletableFuture.
     */
    public static <E, V> CompletableFuture<Result<E, V>> fromFuture(
            CompletableFuture<V> future,
            Fn<Throwable, E> errorMapper) {
        return future
                .thenApply(Result::<E, V>ok)
                .exceptionally(t -> err(errorMapper.apply(t)));
    }

    // ========================================================================
    // QUERY METHODS
    // ========================================================================

    /**
     * Returns true if this is a Success.
     */
    public abstract boolean isOk();

    /**
     * Returns true if this is a Failure.
     */
    public abstract boolean isErr();

    /**
     * Returns true if this Success contains the given value.
     */
    public abstract boolean contains(V value);

    /**
     * Returns true if this Failure contains the given error.
     */
    public abstract boolean containsErr(E error);

    // ========================================================================
    // VALIDATION
    // ========================================================================

    /**
     * Validates the success value against a predicate.
     * Converts to Failure if predicate returns false.
     */
    public abstract Result<E, V> ensure(Predicate<? super V> predicate, E errorIfFalse);

    /**
     * Filters the success value with a predicate.
     * Converts to Failure if predicate returns false.
     */
    public abstract Result<E, V> filter(Predicate<? super V> predicate, E errorIfFalse);

    /**
     * Filters the success value with a negated predicate.
     * Converts to Failure if predicate returns true.
     */
    public abstract Result<E, V> filterNot(Predicate<? super V> predicate, E errorIfTrue);

    // ========================================================================
    // TRANSFORMATIONS
    // ========================================================================

    /**
     * Transforms the success value using the given function.
     */
    public abstract <U> Result<E, U> map(Fn<? super V, ? extends U> mapper);

    /**
     * Transforms the success value into a new Result (monadic bind).
     */
    public abstract <U> Result<E, U> flatMap(Fn<? super V, ? extends Result<E, U>> mapper);

    /**
     * Transforms the error value using the given function.
     */
    public abstract <F> Result<F, V> mapErr(Fn<? super E, ? extends F> mapper);

    /**
     * Transforms both error and value simultaneously.
     */
    public abstract <F, U> Result<F, U> bimap(
            Fn<? super E, ? extends F> errorMapper,
            Fn<? super V, ? extends U> valueMapper
    );

    /**
     * Combines this Result with another using the provided function.
     */
    public abstract <U, R> Result<E, R> zip(
            Result<E, U> other,
            BiFunction<? super V, ? super U, ? extends R> combiner
    );

    // ========================================================================
    // SIDE EFFECTS
    // ========================================================================

    /**
     * Executes a side effect if this is a Success.
     */
    public abstract Result<E, V> peek(Consumer<? super V> consumer);

    /**
     * Executes a side effect if this is a Failure.
     */
    public abstract Result<E, V> peekErr(Consumer<? super E> consumer);

    /**
     * Executes appropriate side effect based on Success or Failure.
     */
    public abstract Result<E, V> tap(Consumer<? super V> onSuccess, Consumer<? super E> onError);

    /**
     * Executes a side effect on success without returning the Result.
     */
    public abstract void ifOk(Consumer<? super V> consumer);

    /**
     * Executes a side effect on failure without returning the Result.
     */
    public abstract void ifErr(Consumer<? super E> consumer);

    // ========================================================================
    // RECOVERY
    // ========================================================================

    /**
     * Recovers from a Failure by generating a success value from the error.
     */
    public abstract Result<E, V> recover(Fn<? super E, ? extends V> recoveryFn);

    /**
     * Recovers from a Failure by returning an alternative Result.
     */
    public abstract Result<E, V> or(Supplier<Result<E, V>> alternative);

    /**
     * Recovers from a Failure by returning an alternative Result.
     */
    public abstract Result<E, V> orElse(Result<E, V> alternative);

    // ========================================================================
    // TERMINAL OPERATIONS
    // ========================================================================

    /**
     * Forces explicit handling of both Success and Failure cases.
     */
    public abstract <R> R fold(
            Fn<? super E, ? extends R> errorMapper,
            Fn<? super V, ? extends R> valueMapper
    );

    /**
     * Returns the success value or a default value if Failure.
     */
    public abstract V getOrElse(V defaultValue);

    /**
     * Returns the success value or computes a default from a supplier.
     */
    public abstract V getOrElseGet(Supplier<? extends V> defaultSupplier);

    /**
     * Returns the error value or a default error if Success.
     */
    public abstract E getErrOrElse(E defaultError);

    /**
     * Returns the error value or computes a default from a supplier.
     */
    public abstract E getErrOrElseGet(Supplier<? extends E> defaultSupplier);

    /**
     * Returns the success value or throws an exception if Failure.
     * UNSAFE: Use only in tests or when failure should crash the program.
     */
    public abstract V getOrThrow();

    /**
     * Returns the error value or throws an exception if Success.
     * Mainly useful for testing.
     */
    public abstract E getErrOrThrow();

    /**
     * Converts this Result to an Optional containing the success value.
     */
    public abstract Optional<V> toOptional();

    /**
     * Converts this Result to an Optional containing the error value.
     */
    public abstract Optional<E> toErrOptional();

    /**
     * Converts this Result to a CompletableFuture.
     */
    public abstract CompletableFuture<V> toFuture();

    // ========================================================================
    // INNER CLASSES
    // ========================================================================

    private static final class Success<E, V> extends Result<E, V> {
        private final V value;

        Success(V value) {
            this.value = value;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public boolean contains(V value) {
            return Objects.equals(this.value, value);
        }

        @Override
        public boolean containsErr(E error) {
            return false;
        }

        @Override
        public Result<E, V> ensure(Predicate<? super V> predicate, E errorIfFalse) {
            return predicate.test(value) ? this : err(errorIfFalse);
        }

        @Override
        public Result<E, V> filter(Predicate<? super V> predicate, E errorIfFalse) {
            return ensure(predicate, errorIfFalse);
        }

        @Override
        public Result<E, V> filterNot(Predicate<? super V> predicate, E errorIfTrue) {
            return predicate.test(value) ? err(errorIfTrue) : this;
        }

        @Override
        public <U> Result<E, U> map(Fn<? super V, ? extends U> mapper) {
            return ok(mapper.apply(value));
        }

        @Override
        public <U> Result<E, U> flatMap(Fn<? super V, ? extends Result<E, U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public <F> Result<F, V> mapErr(Fn<? super E, ? extends F> mapper) {
            return ok(value);
        }

        @Override
        public <F, U> Result<F, U> bimap(
                Fn<? super E, ? extends F> errorMapper,
                Fn<? super V, ? extends U> valueMapper) {
            return ok(valueMapper.apply(value));
        }

        @Override
        public <U, R> Result<E, R> zip(
                Result<E, U> other,
                BiFunction<? super V, ? super U, ? extends R> combiner) {
            return other.map(otherValue -> combiner.apply(value, otherValue));
        }

        @Override
        public Result<E, V> peek(Consumer<? super V> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public Result<E, V> peekErr(Consumer<? super E> consumer) {
            return this;
        }

        @Override
        public Result<E, V> tap(Consumer<? super V> onSuccess, Consumer<? super E> onError) {
            onSuccess.accept(value);
            return this;
        }

        @Override
        public void ifOk(Consumer<? super V> consumer) {
            consumer.accept(value);
        }

        @Override
        public void ifErr(Consumer<? super E> consumer) {
            // No-op
        }

        @Override
        public Result<E, V> recover(Fn<? super E, ? extends V> recoveryFn) {
            return this;
        }

        @Override
        public Result<E, V> or(Supplier<Result<E, V>> alternative) {
            return this;
        }

        @Override
        public Result<E, V> orElse(Result<E, V> alternative) {
            return this;
        }

        @Override
        public <R> R fold(
                Fn<? super E, ? extends R> errorMapper,
                Fn<? super V, ? extends R> valueMapper) {
            return valueMapper.apply(value);
        }

        @Override
        public V getOrElse(V defaultValue) {
            return value;
        }

        @Override
        public V getOrElseGet(Supplier<? extends V> defaultSupplier) {
            return value;
        }

        @Override
        public E getErrOrElse(E defaultError) {
            return defaultError;
        }

        @Override
        public E getErrOrElseGet(Supplier<? extends E> defaultSupplier) {
            return defaultSupplier.get();
        }

        @Override
        public V getOrThrow() {
            return value;
        }

        @Override
        public E getErrOrThrow() {
            throw new ResultException("Cannot get error from Success: " + value);
        }

        @Override
        public Optional<V> toOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public Optional<E> toErrOptional() {
            return Optional.empty();
        }

        @Override
        public CompletableFuture<V> toFuture() {
            return CompletableFuture.completedFuture(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Success)) return false;
            Success<?, ?> other = (Success<?, ?>) obj;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash("Success", value);
        }

        @Override
        public String toString() {
            return "Success(" + value + ")";
        }
    }

    private static final class Failure<E, V> extends Result<E, V> {
        private final E error;

        Failure(E error) {
            this.error = error;
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public boolean contains(V value) {
            return false;
        }

        @Override
        public boolean containsErr(E error) {
            return Objects.equals(this.error, error);
        }

        @Override
        public Result<E, V> ensure(Predicate<? super V> predicate, E errorIfFalse) {
            return this;
        }

        @Override
        public Result<E, V> filter(Predicate<? super V> predicate, E errorIfFalse) {
            return this;
        }

        @Override
        public Result<E, V> filterNot(Predicate<? super V> predicate, E errorIfTrue) {
            return this;
        }

        @Override
        public <U> Result<E, U> map(Fn<? super V, ? extends U> mapper) {
            return err(error);
        }

        @Override
        public <U> Result<E, U> flatMap(Fn<? super V, ? extends Result<E, U>> mapper) {
            return err(error);
        }

        @Override
        public <F> Result<F, V> mapErr(Fn<? super E, ? extends F> mapper) {
            return err(mapper.apply(error));
        }

        @Override
        public <F, U> Result<F, U> bimap(
                Fn<? super E, ? extends F> errorMapper,
                Fn<? super V, ? extends U> valueMapper) {
            return err(errorMapper.apply(error));
        }

        @Override
        public <U, R> Result<E, R> zip(
                Result<E, U> other,
                BiFunction<? super V, ? super U, ? extends R> combiner) {
            return err(error);
        }

        @Override
        public Result<E, V> peek(Consumer<? super V> consumer) {
            return this;
        }

        @Override
        public Result<E, V> peekErr(Consumer<? super E> consumer) {
            consumer.accept(error);
            return this;
        }

        @Override
        public Result<E, V> tap(Consumer<? super V> onSuccess, Consumer<? super E> onError) {
            onError.accept(error);
            return this;
        }

        @Override
        public void ifOk(Consumer<? super V> consumer) {
            // No-op
        }

        @Override
        public void ifErr(Consumer<? super E> consumer) {
            consumer.accept(error);
        }

        @Override
        public Result<E, V> recover(Fn<? super E, ? extends V> recoveryFn) {
            return ok(recoveryFn.apply(error));
        }

        @Override
        public Result<E, V> or(Supplier<Result<E, V>> alternative) {
            return alternative.get();
        }

        @Override
        public Result<E, V> orElse(Result<E, V> alternative) {
            return alternative;
        }

        @Override
        public <R> R fold(
                Fn<? super E, ? extends R> errorMapper,
                Fn<? super V, ? extends R> valueMapper) {
            return errorMapper.apply(error);
        }

        @Override
        public V getOrElse(V defaultValue) {
            return defaultValue;
        }

        @Override
        public V getOrElseGet(Supplier<? extends V> defaultSupplier) {
            return defaultSupplier.get();
        }

        @Override
        public E getErrOrElse(E defaultError) {
            return error;
        }

        @Override
        public E getErrOrElseGet(Supplier<? extends E> defaultSupplier) {
            return error;
        }

        @Override
        public V getOrThrow() {
            throw new ResultException(error);
        }

        @Override
        public E getErrOrThrow() {
            return error;
        }

        @Override
        public Optional<V> toOptional() {
            return Optional.empty();
        }

        @Override
        public Optional<E> toErrOptional() {
            return Optional.of(error);
        }

        @Override
        public CompletableFuture<V> toFuture() {
            CompletableFuture<V> future = new CompletableFuture<>();
            future.completeExceptionally(new ResultException(error));
            return future;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Failure)) return false;
            Failure<?, ?> other = (Failure<?, ?>) obj;
            return Objects.equals(error, other.error);
        }

        @Override
        public int hashCode() {
            return Objects.hash("Failure", error);
        }

        @Override
        public String toString() {
            return "Failure(" + error + ")";
        }
    }
}