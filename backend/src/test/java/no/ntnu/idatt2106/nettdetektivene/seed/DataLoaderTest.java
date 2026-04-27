package no.ntnu.idatt2106.nettdetektivene.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataLoaderTest {

    private final DataLoader dataLoader = new DataLoader(
        null,
        null,
        null,
        new ObjectMapper()
    );

    @Test
    void fakeNewsCorrectAnswerJson_derivesAnswersFromArticleIsRealFlags() {
        String result = dataLoader.fakeNewsCorrectAnswerJson("""
            {
              "articles": [
                { "headline": "A", "isReal": true },
                { "headline": "B", "isReal": false },
                { "headline": "C", "isReal": true }
              ]
            }
            """);

        assertThat(result).isEqualTo("{\"article_0\":true,\"article_1\":false,\"article_2\":true}");
    }

    @Test
    void fakeNewsCorrectAnswerJson_missingArticlesArray_throwsClearException() {
        assertThatThrownBy(() -> dataLoader.fakeNewsCorrectAnswerJson("{\"slides\":[]}"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("missing an 'articles' array");
    }

    @Test
    void run_seedsStopsInChripreStoryOrder_andBossContainsSixSystems() throws Exception {
        StopRepository stopRepository = mock(StopRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        MedalRepository medalRepository = mock(MedalRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DataLoader loader = new DataLoader(stopRepository, taskRepository, medalRepository, objectMapper);

        when(stopRepository.count()).thenReturn(0L);
        when(stopRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(medalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        loader.run(new DefaultApplicationArguments());

        ArgumentCaptor<List<Stop>> stopsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Task>> tasksCaptor = ArgumentCaptor.forClass(List.class);

        org.mockito.Mockito.verify(stopRepository).saveAll(stopsCaptor.capture());
        org.mockito.Mockito.verify(taskRepository).saveAll(tasksCaptor.capture());

        List<Stop> stops = stopsCaptor.getValue();
        assertThat(stops)
            .extracting(Stop::getName, Stop::getOrderIndex)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("Nyhetskvartalet", 1),
                org.assertj.core.groups.Tuple.tuple("Fotografen", 2),
                org.assertj.core.groups.Tuple.tuple("Postkontoret", 3),
                org.assertj.core.groups.Tuple.tuple("Markedsplassen", 4),
                org.assertj.core.groups.Tuple.tuple("Den sosiale møteplassen", 5),
                org.assertj.core.groups.Tuple.tuple("Passordbanken", 6),
                org.assertj.core.groups.Tuple.tuple("Datasenteret", 7)
            );

        Task finalBossTask = tasksCaptor.getValue().stream()
            .filter(task -> task.getTaskType().name().equals("FINAL_BOSS"))
            .findFirst()
            .orElseThrow();

        ArrayNode challenges = (ArrayNode) objectMapper.readTree(finalBossTask.getContentJson()).path("challenges");
        assertThat(challenges).hasSize(6);
        assertThat(challenges)
            .extracting(node -> node.path("type").asText())
            .containsExactly("FAKE_NEWS", "AI_PHOTO", "PHISHING_EMAIL", "MARKETPLACE", "SOCIAL_MEDIA", "PASSWORD");
    }

    @Test
    void run_seedsFakeNewsTasksWithFourArticlesAndOneRealArticle() throws Exception {
        List<Task> tasks = seededTasks();

        List<Task> fakeNewsTasks = tasks.stream()
            .filter(task -> "FAKE_NEWS".equals(task.getTaskType().name()))
            .toList();

        assertThat(fakeNewsTasks).hasSize(3);

        for (Task task : fakeNewsTasks) {
            ArrayNode articles = (ArrayNode) new ObjectMapper().readTree(task.getContentJson()).path("articles");
            assertThat(articles).hasSize(4);
            assertThat(articles)
                .filteredOn(article -> article.path("isReal").asBoolean())
                .hasSize(1);
        }
    }

    @Test
    void run_seedsPhishingTasksWithAtLeastOneSafeClickableElement() throws Exception {
        List<Task> tasks = seededTasks();

        List<Task> phishingTasks = tasks.stream()
            .filter(task -> "PHISHING_EMAIL".equals(task.getTaskType().name()))
            .toList();

        assertThat(phishingTasks).hasSize(3);

        for (Task task : phishingTasks) {
            ArrayNode clues = (ArrayNode) new ObjectMapper().readTree(task.getContentJson()).path("email").path("clues");
            assertThat(clues)
                .filteredOn(clue -> !clue.path("isClue").asBoolean())
                .isNotEmpty();
        }
    }

    @Test
    void run_seedsPasswordBuilderTaskWithPitfalls() throws Exception {
        List<Task> tasks = seededTasks();

        Task builderTask = tasks.stream()
            .filter(task -> "PASSWORD".equals(task.getTaskType().name()))
            .filter(task -> parseJson(task.getContentJson()).path("type").asText().equals("BUILDER"))
            .findFirst()
            .orElseThrow();

        JsonNode content = parseJson(builderTask.getContentJson());
        assertThat(content.path("pitfalls").isArray()).isTrue();
        assertThat(content.path("pitfalls"))
            .extracting(JsonNode::asText)
            .containsExactly("OlaErBest", "2005", "hund");
    }

    private List<Task> seededTasks() throws Exception {
        StopRepository stopRepository = mock(StopRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        MedalRepository medalRepository = mock(MedalRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DataLoader loader = new DataLoader(stopRepository, taskRepository, medalRepository, objectMapper);

        when(stopRepository.count()).thenReturn(0L);
        when(stopRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(medalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        loader.run(new DefaultApplicationArguments());

        ArgumentCaptor<List<Task>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(taskRepository).saveAll(tasksCaptor.capture());
        return tasksCaptor.getValue();
    }

    private JsonNode parseJson(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (Exception e) {
            throw new AssertionError("Failed to parse seeded JSON", e);
        }
    }
}
