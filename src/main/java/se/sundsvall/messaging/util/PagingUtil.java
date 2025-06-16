package se.sundsvall.messaging.util;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.support.PagedListHolder;
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
	 * @param  parameters object containing input for calculating the current requested sub page for the result list
	 * @param  matches    with result to be converted to a paged list
	 * @return            a Page object representing the sublist for the requested page of the list
	 */
	public static Page<Batch> toPage(Integer page, Integer limit, List<Batch> matches) {

		// Convert list into a list of pages
		final var pageList = toPagedListHolder(page, limit, matches);

		if (pageList.getPageCount() < page) {
			return new PageImpl<>(Collections.emptyList(), toPageRequest(page, limit), pageList.getNrOfElements());
		}
		return new PageImpl<>(pageList.getPageList(), PageRequest.of(pageList.getPage(), pageList.getPageSize()), pageList.getNrOfElements());
	}

	static PagedListHolder<Batch> toPagedListHolder(Integer page, Integer limit, List<Batch> matches) {
		final var pageList = new PagedListHolder<>(matches);
		pageList.setPage(page - 1);
		pageList.setPageSize(limit);
		return pageList;
	}

	static PageRequest toPageRequest(Integer page, Integer limit) {
		return PageRequest.of(page - 1, limit);
	}

}
