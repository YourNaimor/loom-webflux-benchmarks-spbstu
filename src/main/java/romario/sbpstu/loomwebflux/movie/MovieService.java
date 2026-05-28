package romario.sbpstu.loomwebflux.movie;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import romario.sbpstu.loomwebflux.common.AbstractService;
import romario.sbpstu.loomwebflux.common.proxy.nonreactive.NonReactiveServiceProxy;
import romario.sbpstu.loomwebflux.common.proxy.reactive.ReactiveServiceProxy;
import romario.sbpstu.loomwebflux.movie.domain.Movie;
import romario.sbpstu.loomwebflux.movie.repo.CachedMovieRepo;

import java.util.List;
import java.util.Set;

import static reactor.core.scheduler.Schedulers.boundedElastic;

@Service
public class MovieService extends AbstractService {

    private final CachedMovieRepo movieRepo;

    MovieService(ReactiveServiceProxy reactiveClient, NonReactiveServiceProxy nonReactiveClient, CachedMovieRepo movieRepo) {
        super(reactiveClient, nonReactiveClient);
        this.movieRepo = movieRepo;
    }

    public Set<Movie> findMoviesByDirectorLastName(String directorLastName, Integer delayCallDepth, Long delayInMillis) throws InterruptedException {
        log("findMoviesByDirectorLastName");
        waitOrFetchEpochMillis(delayCallDepth, delayInMillis);
        return movieRepo.findByDirectorName(directorLastName);
    }

    public Flux<Movie> findMoviesByDirectorLastNameReactive(String directorLastName, Integer delayCallDepth, Long delayInMillis) {
        log("findMoviesByDirectorLastNameReactive");
        return waitOrFetchEpochMillisReactive(delayCallDepth, delayInMillis)
            .thenMany(Flux.defer(() -> Flux.fromIterable(movieRepo.findByDirectorName(directorLastName))));
    }

    public List<Movie> saveMovies(List<Movie> movies, Integer delayCallDepth, @RequestParam Long delayInMillis) throws InterruptedException {
        log("saveMovies");
        waitOrFetchEpochMillis(delayCallDepth, delayInMillis);
        return movieRepo.saveAll(movies);
    }

    public Flux<Movie> saveMoviesReactive(Flux<Movie> movies, Integer delayCallDepth, Long delayInMillis) {
        log("saveMoviesReactive");
        return waitOrFetchEpochMillisReactive(delayCallDepth, delayInMillis)
            .flatMapMany(ignore -> movies
                .publishOn(boundedElastic())
                .collectList()
                .flatMapMany(movieList -> Flux.fromIterable(movieRepo.saveAll(movieList))));
    }

    public void deleteMovieById(Long id, Integer delayCallDepth, Long delayInMillis) throws InterruptedException {
        log("deleteMoviesById");
        waitOrFetchEpochMillis(delayCallDepth, delayInMillis);
        movieRepo.deleteById(id);
    }

    public Mono<Void> deleteMovieByIdReactive(Long id, Integer delayCallDepth, Long delayInMillis) {
        log("deleteMovieByIdReactive");
        return waitOrFetchEpochMillisReactive(delayCallDepth, delayInMillis)
            .then(Mono.fromRunnable(() -> movieRepo.deleteById(id))
                .subscribeOn(boundedElastic()))
            .then();
    }

}
