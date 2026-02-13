package se.sundsvall.messaging.util;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.messaging.api.model.response.Batch;

public class PagingUtil {

	private PagingUtil() {
		// To prevent instantiation
	}

	/**
	 * Method for converting result list into a Page object with sub list for requested page. Convertion must be done
	 * explicitly as stored procedures can not produce a return object of type Page and cant sort result list.
	 *
	 * @param  page    1-based page number
	 * @param  limit   page size
	 * @param  matches with result to be converted to a paged list
	 * @return         a Page object representing the sublist for the requested page of the list
	 */
	public static Page<Batch> toPage(Integer page, Integer limit, List<Batch> matches) {
		final var zeroBasedPage = page - 1;
		final var start = Math.min(zeroBasedPage * limit, matches.size());
		final var end = Math.min(start + limit, matches.size());
		final var content = matches.subList(start, end);
		return new PageImpl<>(content, PageRequest.of(zeroBasedPage, limit), matches.size());
	}

}
