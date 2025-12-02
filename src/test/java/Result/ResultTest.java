package Result;



import commons.kit.ErrorUtils.Empty;
import commons.kit.ErrorUtils.Result;
import commons.kit.ErrorUtils.ResultException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Result")
class ResultTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("ok() creates Success with value")
        void okCreatesSuccess() {
            Result<String, Integer> result = Result.ok(42);

            assertThat(result.isOk()).isTrue();
            assertThat(result.isErr()).isFalse();
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("ok() accepts null value")
        void okAcceptsNull() {
            Result<String, String> result = Result.ok(null);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrElse("default")).isNull();
        }

        @Test
        @DisplayName("err() creates Failure with error")
        void errCreatesFailure() {
            Result<String, Integer> result = Result.err("error");

            assertThat(result.isErr()).isTrue();
            assertThat(result.isOk()).isFalse();
            assertThat(result.getErrOrElse("default")).isEqualTo("error");
        }

        @Test
        @DisplayName("err() throws on null error")
        void errRejectsNull() {
            assertThatThrownBy(() -> Result.err(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("of() wraps successful operation")
        void ofWrapsSuccess() {
            Result<Throwable, Integer> result = Result.of(() -> 42);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("of() wraps throwing operation")
        void ofWrapsException() {
            Result<Throwable, Integer> result = Result.of(() -> {
                throw new RuntimeException("boom");
            });

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrThrow()).isInstanceOf(RuntimeException.class);
            assertThat(result.getErrOrThrow().getMessage()).isEqualTo("boom");
        }

        @Test
        @DisplayName("of() with mapper converts exception to custom error")
        void ofWithMapperConvertsException() {
            Result<String, Integer> result = Result.of(
                    () -> Integer.parseInt("not-a-number"),
                    ex -> "Parse error: " + ex.getMessage()
            );

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrElse("")).startsWith("Parse error:");
        }

        @Test
        @DisplayName("sequence() combines successful results")
        void sequenceWithAllSuccess() {
            List<Result<String, Integer>> results = Arrays.asList(
                    Result.ok(1),
                    Result.ok(2),
                    Result.ok(3)
            );

            Result<String, List<Integer>> combined = Result.sequence(results);

            assertThat(combined.isOk()).isTrue();
            assertThat(combined.getOrElse(null)).isEqualTo(Arrays.asList(1, 2, 3));
        }

        @Test
        @DisplayName("sequence() returns first failure")
        void sequenceWithFailure() {
            List<Result<String, Integer>> results = Arrays.asList(
                    Result.ok(1),
                    Result.err("error1"),
                    Result.err("error2")
            );

            Result<String, List<Integer>> combined = Result.sequence(results);

            assertThat(combined.isErr()).isTrue();
            assertThat(combined.getErrOrElse("")).isEqualTo("error1");
        }

        @Test
        @DisplayName("flatten() unwraps nested Result")
        void flattenUnwrapsNested() {
            Result<String, Result<String, Integer>> nested = Result.ok(Result.ok(42));
            Result<String, Integer> flattened = Result.flatten(nested);

            assertThat(flattened.isOk()).isTrue();
            assertThat(flattened.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("flatten() handles nested failure")
        void flattenWithNestedFailure() {
            Result<String, Result<String, Integer>> nested = Result.ok(Result.err("inner error"));
            Result<String, Integer> flattened = Result.flatten(nested);

            assertThat(flattened.isErr()).isTrue();
            assertThat(flattened.getErrOrElse("")).isEqualTo("inner error");
        }
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryMethods {

        @Test
        @DisplayName("contains() checks success value equality")
        void containsChecksValue() {
            Result<String, Integer> result = Result.ok(42);

            assertThat(result.contains(42)).isTrue();
            assertThat(result.contains(99)).isFalse();
        }

        @Test
        @DisplayName("contains() returns false for Failure")
        void containsReturnsFalseForFailure() {
            Result<String, Integer> result = Result.err("error");

            assertThat(result.contains(42)).isFalse();
        }

        @Test
        @DisplayName("containsErr() checks error equality")
        void containsErrChecksError() {
            Result<String, Integer> result = Result.err("error");

            assertThat(result.containsErr("error")).isTrue();
            assertThat(result.containsErr("other")).isFalse();
        }

        @Test
        @DisplayName("containsErr() returns false for Success")
        void containsErrReturnsFalseForSuccess() {
            Result<String, Integer> result = Result.ok(42);

            assertThat(result.containsErr("error")).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation Methods")
    class ValidationMethods {

        @Test
        @DisplayName("ensure() validates success value")
        void ensureValidatesValue() {
            Result<String, Integer> result = Result.<String, Integer>ok(20)
                    .ensure(x -> x >= 18, "Must be 18+");

            assertThat(result.isOk()).isTrue();
        }

        @Test
        @DisplayName("ensure() converts to failure on validation failure")
        void ensureConvertsToFailure() {
            Result<String, Integer> result = Result.<String, Integer>ok(15)
                    .ensure(x -> x >= 18, "Must be 18+");

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrElse("")).isEqualTo("Must be 18+");
        }

        @Test
        @DisplayName("ensure() short-circuits on existing failure")
        void ensureShortCircuits() {
            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .ensure(x -> x >= 18, "Must be 18+");

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrElse("")).isEqualTo("error");
        }

        @Test
        @DisplayName("filter() behaves like ensure()")
        void filterBehavesLikeEnsure() {
            Result<String, Integer> pass = Result.<String, Integer>ok(20)
                    .filter(x -> x >= 18, "Must be 18+");
            Result<String, Integer> fail = Result.<String, Integer>ok(15)
                    .filter(x -> x >= 18, "Must be 18+");

            assertThat(pass.isOk()).isTrue();
            assertThat(fail.isErr()).isTrue();
        }

        @Test
        @DisplayName("filterNot() inverts the predicate")
        void filterNotInvertsPredicate() {
            Result<String, Integer> start = Result.ok(15);
            Result<String, Integer> result = start.filterNot(x -> x >= 18, "Must be under 18");
            assertThat(result.isOk()).isTrue();

            Result<String, Integer> adult = Result.<String, Integer>ok(20)
                    .filterNot(x -> x >= 18, "Must be under 18");

            assertThat(adult.isErr()).isTrue();
        }
    }

    @Nested
    @DisplayName("Transformation Methods")
    class TransformationMethods {

        @Test
        @DisplayName("map() transforms success value")
        void mapTransformsValue() {
            Result<String, Integer> result = Result.<String, Integer>ok(5)
                    .map(x -> x * 2);

            assertThat(result.getOrElse(0)).isEqualTo(10);
        }

        @Test
        @DisplayName("map() chains multiple transformations")
        void mapChains() {
            Result<String, Integer> result = Result.<String, Integer>ok(5)
                    .map(x -> x * 2)
                    .map(x -> x + 3);

            assertThat(result.getOrElse(0)).isEqualTo(13);
        }

        @Test
        @DisplayName("map() short-circuits on failure")
        void mapShortCircuits() {
            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .map(x -> x * 2);

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrElse("")).isEqualTo("error");
        }

        @Test
        @DisplayName("flatMap() chains operations that return Result")
        void flatMapChains() {
            Result<String, Integer> result = Result.<String, Integer>ok(5)
                    .flatMap(x -> Result.ok(x * 2))
                    .flatMap(x -> Result.ok(x + 3));

            assertThat(result.getOrElse(0)).isEqualTo(13);
        }

        @Test
        @DisplayName("flatMap() propagates inner failure")
        void flatMapPropagatesFailure() {
            Result<String, Integer> result = Result.<String, Integer>ok(5)
                    .flatMap(x -> Result.err("inner error"));

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrElse("")).isEqualTo("inner error");
        }

        @Test
        @DisplayName("mapErr() transforms error type")
        void mapErrTransformsError() {
            Result<String, Integer> result = Result.<Integer, Integer>err(404)
                    .mapErr(code -> "Error code: " + code);

            assertThat(result.isErr()).isTrue();
            assertThat(result.getErrOrElse("")).isEqualTo("Error code: 404");
        }

        @Test
        @DisplayName("mapErr() doesn't affect success")
        void mapErrLeavesSuccessUnchanged() {
            Result<String, Integer> result = Result.<Integer, Integer>ok(42)
                    .mapErr(code -> "Error code: " + code);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("bimap() transforms both sides")
        void bimapTransformsBoth() {
            Result<String, Integer> success = Result.<Integer, Integer>ok(42)
                    .bimap(
                            code -> "Error: " + code,
                            value -> value * 2
                    );

            Result<String, Integer> failure = Result.<Integer, Integer>err(404)
                    .bimap(
                            code -> "Error: " + code,
                            value -> value * 2
                    );

            assertThat(success.getOrElse(0)).isEqualTo(84);
            assertThat(failure.getErrOrElse("")).isEqualTo("Error: 404");
        }

        @Test
        @DisplayName("zip() combines two successful results")
        void zipCombinesSuccess() {
            Result<String, Integer> r1 = Result.ok(10);
            Result<String, Integer> r2 = Result.ok(5);

            Result<String, Integer> combined = r1.zip(r2, (a, b) -> a + b);

            assertThat(combined.getOrElse(0)).isEqualTo(15);
        }

        @Test
        @DisplayName("zip() propagates first failure")
        void zipPropagatesFailure() {
            Result<String, Integer> r1 = Result.err("error1");
            Result<String, Integer> r2 = Result.ok(5);

            Result<String, Integer> combined = r1.zip(r2, (a, b) -> a + b);

            assertThat(combined.isErr()).isTrue();
            assertThat(combined.getErrOrElse("")).isEqualTo("error1");
        }
    }

    @Nested
    @DisplayName("Side Effect Methods")
    class SideEffectMethods {

        @Test
        @DisplayName("peek() executes on success")
        void peekExecutesOnSuccess() {
            AtomicInteger counter = new AtomicInteger(0);

            Result<String, Integer> result = Result.<String, Integer>ok(42)
                    .peek(x -> counter.set(x));

            assertThat(counter.get()).isEqualTo(42);
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("peek() doesn't execute on failure")
        void peekSkipsFailure() {
            AtomicBoolean executed = new AtomicBoolean(false);

            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .peek(x -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("peekErr() executes on failure")
        void peekErrExecutesOnFailure() {
            AtomicBoolean executed = new AtomicBoolean(false);

            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .peekErr(e -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("peekErr() doesn't execute on success")
        void peekErrSkipsSuccess() {
            AtomicBoolean executed = new AtomicBoolean(false);

            Result<String, Integer> result = Result.<String, Integer>ok(42)
                    .peekErr(e -> executed.set(true));

            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("tap() executes appropriate callback")
        void tapExecutesAppropriateCallback() {
            AtomicBoolean successCalled = new AtomicBoolean(false);
            AtomicBoolean errorCalled = new AtomicBoolean(false);

            Result.ok(42).tap(
                    v -> successCalled.set(true),
                    e -> errorCalled.set(true)
            );

            assertThat(successCalled.get()).isTrue();
            assertThat(errorCalled.get()).isFalse();

            successCalled.set(false);
            Result.<String, Integer>err("error").tap(
                    v -> successCalled.set(true),
                    e -> errorCalled.set(true)
            );

            assertThat(successCalled.get()).isFalse();
            assertThat(errorCalled.get()).isTrue();
        }

        @Test
        @DisplayName("ifOk() executes only on success")
        void ifOkExecutesOnSuccess() {
            AtomicInteger value = new AtomicInteger(0);

            Result.ok(42).ifOk(value::set);
            assertThat(value.get()).isEqualTo(42);

            value.set(0);
            Result.<String, Integer>err("error").ifOk(value::set);
            assertThat(value.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("ifErr() executes only on failure")
        void ifErrExecutesOnFailure() {
            AtomicBoolean executed = new AtomicBoolean(false);

            Result.<String, Integer>err("error").ifErr(e -> executed.set(true));
            assertThat(executed.get()).isTrue();

            executed.set(false);
            Result.ok(42).ifErr(e -> executed.set(true));
            assertThat(executed.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Recovery Methods")
    class RecoveryMethods {

        @Test
        @DisplayName("recover() converts failure to success")
        void recoverConvertsFailure() {
            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .recover(e -> 0);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrElse(-1)).isEqualTo(0);
        }

        @Test
        @DisplayName("recover() doesn't affect success")
        void recoverLeavesSuccessUnchanged() {
            Result<String, Integer> result = Result.<String, Integer>ok(42)
                    .recover(e -> 0);

            assertThat(result.getOrElse(-1)).isEqualTo(42);
        }

        @Test
        @DisplayName("or() returns alternative on failure")
        void orReturnsAlternative() {
            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .or(() -> Result.ok(99));

            assertThat(result.getOrElse(0)).isEqualTo(99);
        }

        @Test
        @DisplayName("or() doesn't evaluate alternative on success")
        void orSkipsAlternativeOnSuccess() {
            AtomicBoolean evaluated = new AtomicBoolean(false);

            Result<String, Integer> result = Result.<String, Integer>ok(42)
                    .or(() -> {
                        evaluated.set(true);
                        return Result.ok(99);
                    });

            assertThat(evaluated.get()).isFalse();
            assertThat(result.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("orElse() returns alternative eagerly")
        void orElseReturnsAlternative() {
            Result<String, Integer> result = Result.<String, Integer>err("error")
                    .orElse(Result.ok(99));

            assertThat(result.getOrElse(0)).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("Terminal Operations")
    class TerminalOperations {

        @Test
        @DisplayName("fold() handles both cases")
        void foldHandlesBothCases() {
            String success = Result.ok(42).fold(
                    e -> "Error: " + e,
                    v -> "Value: " + v
            );

            String failure = Result.<String, Integer>err("error").fold(
                    e -> "Error: " + e,
                    v -> "Value: " + v
            );

            assertThat(success).isEqualTo("Value: 42");
            assertThat(failure).isEqualTo("Error: error");
        }

        @Test
        @DisplayName("getOrElse() returns value or default")
        void getOrElseReturnsValueOrDefault() {
            assertThat(Result.ok(42).getOrElse(0)).isEqualTo(42);
            assertThat(Result.<String, Integer>err("error").getOrElse(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("getOrElseGet() uses supplier lazily")
        void getOrElseGetUsesSupplierLazily() {
            AtomicBoolean evaluated = new AtomicBoolean(false);

            Result.ok(42).getOrElseGet(() -> {
                evaluated.set(true);
                return 0;
            });

            assertThat(evaluated.get()).isFalse();

            Result.<String, Integer>err("error").getOrElseGet(() -> {
                evaluated.set(true);
                return 0;
            });

            assertThat(evaluated.get()).isTrue();
        }

        @Test
        @DisplayName("getErrOrElse() returns error or default")
        void getErrOrElseReturnsErrorOrDefault() {
            assertThat(Result.<String, Integer>err("error").getErrOrElse("default"))
                    .isEqualTo("error");
            assertThat(Result.ok(42).getErrOrElse("default"))
                    .isEqualTo("default");
        }

        @Test
        @DisplayName("getErrOrElseGet() uses supplier lazily")
        void getErrOrElseGetUsesSupplierLazily() {
            AtomicBoolean evaluated = new AtomicBoolean(false);

            Result.<String, Integer>err("error").getErrOrElseGet(() -> {
                evaluated.set(true);
                return "default";
            });

            assertThat(evaluated.get()).isFalse();

            Result.ok(42).getErrOrElseGet(() -> {
                evaluated.set(true);
                return "default";
            });

            assertThat(evaluated.get()).isTrue();
        }

        @Test
        @DisplayName("getOrThrow() returns value on success")
        void getOrThrowReturnsValue() {
            assertThat(Result.ok(42).getOrThrow()).isEqualTo(42);
        }

        @Test
        @DisplayName("getOrThrow() throws on failure")
        void getOrThrowThrowsOnFailure() {
            Result<String, Integer> result = Result.err("error");

            assertThatThrownBy(result::getOrThrow)
                    .isInstanceOf(ResultException.class)
                    .hasMessageContaining("error");
        }

        @Test
        @DisplayName("getErrOrThrow() returns error on failure")
        void getErrOrThrowReturnsError() {
            assertThat(Result.<String, Integer>err("error").getErrOrThrow())
                    .isEqualTo("error");
        }

        @Test
        @DisplayName("getErrOrThrow() throws on success")
        void getErrOrThrowThrowsOnSuccess() {
            Result<String, Integer> result = Result.ok(42);

            assertThatThrownBy(result::getErrOrThrow)
                    .isInstanceOf(ResultException.class)
                    .hasMessageContaining("Cannot get error from Success");
        }

        @Test
        @DisplayName("toOptional() converts success to present Optional")
        void toOptionalConvertsSuccess() {
            Optional<Integer> opt = Result.ok(42).toOptional();

            assertThat(opt).isPresent();
            assertThat(opt.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toOptional() converts failure to empty Optional")
        void toOptionalConvertsFailure() {
            Optional<Integer> opt = Result.<String, Integer>err("error").toOptional();

            assertThat(opt).isEmpty();
        }

        @Test
        @DisplayName("toErrOptional() converts failure to present Optional")
        void toErrOptionalConvertsFailure() {
            Optional<String> opt = Result.<String, Integer>err("error").toErrOptional();

            assertThat(opt).isPresent();
            assertThat(opt.get()).isEqualTo("error");
        }

        @Test
        @DisplayName("toErrOptional() converts success to empty Optional")
        void toErrOptionalConvertsSuccess() {
            Optional<String> opt = Result.<String, Integer>ok(42).toErrOptional();

            assertThat(opt).isEmpty();
        }

        @Test
        @DisplayName("toFuture() converts success to completed future")
        void toFutureConvertsSuccess() {
            CompletableFuture<Integer> future = Result.ok(42).toFuture();

            assertThat(future).isCompleted();
            assertThat(future.join()).isEqualTo(42);
        }

        @Test
        @DisplayName("toFuture() converts failure to failed future")
        void toFutureConvertsFailure() {
            CompletableFuture<Integer> future = Result.<String, Integer>err("error").toFuture();

            assertThat(future).isCompletedExceptionally();
            assertThatThrownBy(future::join)
                    .hasCauseInstanceOf(ResultException.class);
        }
    }

    @Nested
    @DisplayName("Real World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("User validation pipeline")
        void userValidationPipeline() {
            Result<String, User> result = Result.<String, User>ok(new User("John", 25, "john@example.com"))
                    .ensure(u -> u.age >= 18, "User must be 18+")
                    .ensure(u -> u.email.contains("@"), "Invalid email")
                    .map(u -> new User(u.name.toUpperCase(), u.age, u.email));

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrThrow().name).isEqualTo("JOHN");
        }

        @Test
        @DisplayName("Multi-step operation with recovery")
        void multiStepWithRecovery() {
            Result<String, Integer> result = Result.of(() -> fetchData(), Throwable::getMessage)
                    .flatMap(this::processData)
                    .map(processed -> processed.length() * 2)
                    .recover(error -> 0);

            assertThat(result.isOk()).isTrue();
        }

        @Test
        @DisplayName("Chained API calls with fallback")
        void chainedApiCallsWithFallback() {
            Result<String, String> result = fetchFromPrimary()
                    .or(() -> fetchFromSecondary())
                    .or(() -> fetchFromCache())
                    .recover(err -> "default-value");

            assertThat(result.isOk()).isTrue();
        }

        @Test
        @DisplayName("Combining multiple results")
        void combiningMultipleResults() {
            Result<String, Integer> r1 = Result.ok(10);
            Result<String, Integer> r2 = Result.ok(20);
            Result<String, Integer> r3 = Result.ok(30);

            Result<String, Integer> sum = r1.zip(r2, Integer::sum)
                    .zip(r3, Integer::sum);

            assertThat(sum.getOrElse(0)).isEqualTo(60);
        }

        // Helper methods for scenarios
        private String fetchData() {
            return "data";
        }

        private Result<String, String> processData(String data) {
            return Result.ok(data.toUpperCase());
        }

        private Result<String, String> fetchFromPrimary() {
            return Result.err("primary down");
        }

        private Result<String, String> fetchFromSecondary() {
            return Result.err("secondary down");
        }

        private Result<String, String> fetchFromCache() {
            return Result.ok("cached-value");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Null value in Success")
        void nullValueInSuccess() {
            Result<String, String> result = Result.ok(null);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrElse("default")).isNull();
            assertThat(result.toOptional()).isEmpty();
        }

        @Test
        @DisplayName("Equals and hashCode consistency")
        void equalsAndHashCode() {
            Result<String, Integer> r1 = Result.ok(42);
            Result<String, Integer> r2 = Result.ok(42);
            Result<String, Integer> r3 = Result.ok(99);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
            assertThat(r1).isNotEqualTo(r3);

            Result<String, Integer> e1 = Result.err("error");
            Result<String, Integer> e2 = Result.err("error");

            assertThat(e1).isEqualTo(e2);
            assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        }

        @Test
        @DisplayName("toString() provides useful representation")
        void toStringRepresentation() {
            assertThat(Result.ok(42).toString()).isEqualTo("Success(42)");
            assertThat(Result.err("error").toString()).isEqualTo("Failure(error)");
        }

        @Test
        @DisplayName("Empty result handling")
        void emptyResultHandling() {
            Result<String, Empty> result = Result.ok(Empty.INSTANCE);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getOrElse(null)).isEqualTo(Empty.INSTANCE);
            assertThat(result.toString()).contains("Empty");
        }
    }

    // Helper class for testing
    static class User {
        final String name;
        final int age;
        final String email;

        User(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }
    }
}