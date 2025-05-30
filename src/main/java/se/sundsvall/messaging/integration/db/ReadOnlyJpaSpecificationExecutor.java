package se.sundsvall.messaging.integration.db;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.Nullable;

@NoRepositoryBean
public interface ReadOnlyJpaSpecificationExecutor<T, ID> extends Repository<T, ID> {

	/**
	 * Returns a single entity matching the given {@link Specification} or {@link Optional#empty()} if none found.
	 *
	 * @param  spec                                                           must not be {@literal null}.
	 * @return                                                                never {@literal null}.
	 * @throws org.springframework.dao.IncorrectResultSizeDataAccessException if more than one entity found.
	 */
	Optional<T> findOne(Specification<T> spec);

	/**
	 * Returns all entities matching the given {@link Specification}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be selected.
	 *
	 * @param  spec can be {@literal null}.
	 * @return      never {@literal null}.
	 */
	List<T> findAll(@Nullable Specification<T> spec);

	/**
	 * Returns a {@link Page} of entities matching the given {@link Specification}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be selected.
	 *
	 * @param  spec     can be {@literal null}.
	 * @param  pageable must not be {@literal null}.
	 * @return          never {@literal null}.
	 */
	Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);

	/**
	 * Returns all entities matching the given {@link Specification} and {@link Sort}.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be selected.
	 *
	 * @param  spec can be {@literal null}.
	 * @param  sort must not be {@literal null}.
	 * @return      never {@literal null}.
	 */
	List<T> findAll(@Nullable Specification<T> spec, Sort sort);

	/**
	 * Returns the number of instances that the given {@link Specification} will return.
	 * <p>
	 * If no {@link Specification} is given all entities matching {@code <T>} will be counted.
	 *
	 * @param  spec the {@link Specification} to count instances for, must not be {@literal null}.
	 * @return      the number of instances.
	 */
	long count(@Nullable Specification<T> spec);

	/**
	 * Checks whether the data store contains elements that match the given {@link Specification}.
	 *
	 * @param  spec the {@link Specification} to use for the existence check, ust not be {@literal null}.
	 * @return      {@code true} if the data store contains elements that match the given {@link Specification} otherwise
	 *              {@code false}.
	 */
	boolean exists(Specification<T> spec);

	/**
	 * Returns entities matching the given {@link Specification} applying the {@code queryFunction} that defines the query
	 * and its result type.
	 * <p>
	 * The query object used with {@code queryFunction} is only valid inside the {@code findBy(â€¦)} method call. This
	 * requires the query function to return a query result and not the {@link FluentQuery} object itself to ensure the
	 * query is executed inside the {@code findBy(â€¦)} method.
	 *
	 * @param  spec                               must not be null.
	 * @param  queryFunction                      the query function defining projection, sorting, and the result type
	 * @return                                    all entities matching the given specification.
	 * @since                                     3.0
	 * @throws InvalidDataAccessApiUsageException if the query function returns the {@link FluentQuery} instance.
	 */
	<S extends T, R> R findBy(Specification<T> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);

}
