package no.ntnu.idatt2106.nettdetektivene.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import no.ntnu.idatt2106.nettdetektivene.entity.Medal;
import no.ntnu.idatt2106.nettdetektivene.entity.Stop;
import no.ntnu.idatt2106.nettdetektivene.entity.Task;
import no.ntnu.idatt2106.nettdetektivene.entity.TaskType;
import no.ntnu.idatt2106.nettdetektivene.repository.MedalRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.StopRepository;
import no.ntnu.idatt2106.nettdetektivene.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.Map;

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

        ArgumentCaptor<List<Stop>> stopsCaptor = listCaptor();
        ArgumentCaptor<List<Task>> tasksCaptor = listCaptor();

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
                .filteredOn(article -> !article.path("isReal").asBoolean())
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
    void run_seedsPasswordBuilderTaskWithoutPitfalls() throws Exception {
        List<Task> tasks = seededTasks();

        Task builderTask = tasks.stream()
            .filter(task -> "PASSWORD".equals(task.getTaskType().name()))
            .filter(task -> parseJson(task.getContentJson()).path("type").asText().equals("BUILDER"))
            .findFirst()
            .orElseThrow();

        JsonNode content = parseJson(builderTask.getContentJson());
        assertThat(content.has("pitfalls")).isFalse();
    }

    @Test
    void run_seedsPasswordLearningQuizWithVariedCorrectOptionPositions() throws Exception {
        List<Task> tasks = seededTasks();

        Task learnTask = tasks.stream()
            .filter(task -> "LEARN".equals(task.getTaskType().name()))
            .filter(task -> task.getStop().getName().equals("Passordbanken"))
            .findFirst()
            .orElseThrow();

        ArrayNode quiz = (ArrayNode) parseJson(learnTask.getContentJson()).path("quiz");

        assertThat(correctOptionIndex(quiz.get(0))).isEqualTo(2);
        assertThat(correctOptionIndex(quiz.get(1))).isEqualTo(1);
        assertThat(correctOptionIndex(quiz.get(2))).isEqualTo(0);
    }

    @Test
    void run_seedsPasswordBuilderTaskWithMaxLength() throws Exception {
        List<Task> tasks = seededTasks();

        Task builderTask = tasks.stream()
            .filter(task -> "PASSWORD".equals(task.getTaskType().name()))
            .filter(task -> parseJson(task.getContentJson()).path("type").asText().equals("BUILDER"))
            .findFirst()
            .orElseThrow();

        JsonNode content = parseJson(builderTask.getContentJson());
        assertThat(content.path("maxLength").asInt()).isEqualTo(24);
    }

    @Test
    void run_seedsPasswordBuilderTaskUsesNeutralNumberOptions() throws Exception {
        List<Task> tasks = seededTasks();

        Task builderTask = tasks.stream()
            .filter(task -> "PASSWORD".equals(task.getTaskType().name()))
            .filter(task -> parseJson(task.getContentJson()).path("type").asText().equals("BUILDER"))
            .findFirst()
            .orElseThrow();

        ArrayNode numbers = (ArrayNode) parseJson(builderTask.getContentJson()).path("numbers");
        assertThat(numbers).extracting(JsonNode::asText)
            .contains("67")
            .doesNotContain("7");
    }

    @Test
    void run_seedsPasswordBankWithThreePasswordTasksAndFinalClueRiddle() throws Exception {
        List<Task> tasks = seededTasks();

        List<Task> passwordBankTasks = tasks.stream()
            .filter(task -> task.getStop().getName().equals("Passordbanken"))
            .toList();

        assertThat(passwordBankTasks)
            .filteredOn(task -> !"LEARN".equals(task.getTaskType().name()))
            .hasSize(4);
        assertThat(passwordBankTasks)
            .filteredOn(task -> "PASSWORD".equals(task.getTaskType().name()))
            .hasSize(3);
        assertThat(passwordBankTasks)
            .filteredOn(task -> "CLUE_RIDDLE".equals(task.getTaskType().name()))
            .singleElement()
            .satisfies(task -> {
                assertThat(task.getOrderIndex()).isEqualTo(5);
                assertThat(task.getTitle()).isEqualTo("Gåtespor: Siste spor");
            });
    }

    @Test
    void run_seedsPasswordImprovementTaskWithUpdatedCorrectAnswerAndFeedback() throws Exception {
        List<Task> tasks = seededTasks();

        Task passwordImprovementTask = tasks.stream()
            .filter(task -> task.getStop().getName().equals("Passordbanken"))
            .filter(task -> task.getOrderIndex() == 3)
            .findFirst()
            .orElseThrow();

        JsonNode content = parseJson(passwordImprovementTask.getContentJson());
        JsonNode answer = parseJson(passwordImprovementTask.getCorrectAnswerJson());

        assertThat(passwordImprovementTask.getTitle()).isEqualTo("Gjør passordet bedre");
        assertThat(content.path("question").asText())
            .isEqualTo("Noen har prøvd å gjøre passordet 'HeiPåDeg' sterkere. Hvilken versjon er best?");
        assertThat(content.path("options").get(1).path("value").asText()).isEqualTo("H@iPÅD4g!021");
        assertThat(answer.path("selected").asText()).isEqualTo("b");
        assertThat(content.path("explanation").asText())
            .contains("mindre personlig")
            .contains("mer tilfeldig")
            .doesNotContain("Ã");
    }

    @Test
    void run_seedsPasswordClueRiddleWithExplicitEvidencePassword() throws Exception {
        List<Task> tasks = seededTasks();

        Task clueTask = tasks.stream()
            .filter(task -> task.getStop().getName().equals("Passordbanken"))
            .filter(task -> "CLUE_RIDDLE".equals(task.getTaskType().name()))
            .findFirst()
            .orElseThrow();

        JsonNode content = parseJson(clueTask.getContentJson());
        assertThat(content.path("evidencePassword").asText()).isEqualTo("Muserbest123");
        assertThat(content.path("variant").asText()).isEqualTo("password");
    }

    @Test
    void run_updatesExistingSeededTasksWhenDatabaseAlreadyContainsStops() throws Exception {
        List<Task> seededTasks = seededTasks();
        Map<Integer, String> expectedAnswersByOrder = seededTasks.stream()
            .filter(task -> task.getStop().getName().equals("Passordbanken"))
            .filter(task -> "PASSWORD".equals(task.getTaskType().name()))
            .collect(java.util.stream.Collectors.toMap(Task::getOrderIndex, Task::getCorrectAnswerJson));

        StopRepository stopRepository = mock(StopRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        MedalRepository medalRepository = mock(MedalRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DataLoader loader = new DataLoader(stopRepository, taskRepository, medalRepository, objectMapper);

        Stop passwordStop = new Stop();
        passwordStop.setId(6L);
        passwordStop.setName("Passordbanken");
        passwordStop.setOrderIndex(6);
        passwordStop.setTheme("PASSWORD");

        Task order2 = new Task();
        order2.setId(102L);
        order2.setStop(passwordStop);
        order2.setOrderIndex(2);
        order2.setTaskType(TaskType.PASSWORD);
        order2.setTitle("Velg det tryggeste passordet");
        order2.setCorrectAnswerJson("{\"selected\":\"legacy\"}");
        order2.setContentJson("{\"type\":\"CHOICE\",\"question\":\"old\"}");

        Task order3 = new Task();
        order3.setId(103L);
        order3.setStop(passwordStop);
        order3.setOrderIndex(3);
        order3.setTaskType(TaskType.PASSWORD);
        order3.setTitle("Gjør passordet bedre");
        order3.setCorrectAnswerJson("{\"selected\":\"legacy\"}");
        order3.setContentJson("{\"type\":\"CHOICE\",\"question\":\"old\"}");

        when(stopRepository.count()).thenReturn(1L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(passwordStop));
        when(stopRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.count()).thenReturn(2L);
        when(taskRepository.findAll()).thenReturn(List.of(order2, order3));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(medalRepository.count()).thenReturn(0L);
        when(medalRepository.findAll()).thenReturn(List.of());
        when(medalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        loader.run(new DefaultApplicationArguments());

        ArgumentCaptor<List<Task>> tasksCaptor = listCaptor();
        org.mockito.Mockito.verify(taskRepository).saveAll(tasksCaptor.capture());

        Map<Integer, Task> passwordTasksByOrder = tasksCaptor.getValue().stream()
            .filter(task -> "Passordbanken".equals(task.getStop().getName()))
            .filter(task -> "PASSWORD".equals(task.getTaskType().name()))
            .collect(java.util.stream.Collectors.toMap(Task::getOrderIndex, task -> task));

        assertThat(passwordTasksByOrder.get(2).getCorrectAnswerJson()).isEqualTo(expectedAnswersByOrder.get(2));
        assertThat(passwordTasksByOrder.get(3).getCorrectAnswerJson()).isEqualTo(expectedAnswersByOrder.get(3));
        assertThat(passwordTasksByOrder.get(2).getId()).isEqualTo(102L);
        assertThat(passwordTasksByOrder.get(3).getId()).isEqualTo(103L);
    }

    @Test
    void run_updatesExistingSeededStopsWhenDatabaseAlreadyContainsStops() throws Exception {
        StopRepository stopRepository = mock(StopRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        MedalRepository medalRepository = mock(MedalRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DataLoader loader = new DataLoader(stopRepository, taskRepository, medalRepository, objectMapper);

        Stop passwordStop = new Stop();
        passwordStop.setId(6L);
        passwordStop.setName("Old Password Stop");
        passwordStop.setDescription("old");
        passwordStop.setOrderIndex(6);
        passwordStop.setTheme("OLD");
        passwordStop.setFinalBoss(true);

        when(stopRepository.count()).thenReturn(1L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(passwordStop));
        when(stopRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(medalRepository.count()).thenReturn(0L);
        when(medalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        loader.run(new DefaultApplicationArguments());

        ArgumentCaptor<List<Stop>> stopsCaptor = listCaptor();
        org.mockito.Mockito.verify(stopRepository).saveAll(stopsCaptor.capture());

        Stop savedPasswordStop = stopsCaptor.getValue().stream()
            .filter(stop -> stop.getOrderIndex() == 6)
            .findFirst()
            .orElseThrow();

        assertThat(savedPasswordStop.getId()).isEqualTo(6L);
        assertThat(savedPasswordStop.getName()).isEqualTo("Passordbanken");
        assertThat(savedPasswordStop.getTheme()).isEqualTo("PASSWORD");
        assertThat(savedPasswordStop.isFinalBoss()).isFalse();
    }

    @Test
    void run_updatesExistingSeededMedalsWhenDatabaseAlreadyContainsMedals() throws Exception {
        StopRepository stopRepository = mock(StopRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        MedalRepository medalRepository = mock(MedalRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DataLoader loader = new DataLoader(stopRepository, taskRepository, medalRepository, objectMapper);

        Stop passwordStop = new Stop();
        passwordStop.setId(6L);
        passwordStop.setName("Passordbanken");
        passwordStop.setOrderIndex(6);
        passwordStop.setTheme("PASSWORD");

        Medal existingMedal = new Medal();
        existingMedal.setId(16L);
        existingMedal.setName("Passordvokter");
        existingMedal.setDescription("old");
        existingMedal.setImageUrl("/old.png");
        existingMedal.setStop(passwordStop);

        when(stopRepository.count()).thenReturn(1L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(passwordStop));
        when(stopRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.count()).thenReturn(0L);
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(medalRepository.count()).thenReturn(1L);
        when(medalRepository.findAll()).thenReturn(List.of(existingMedal));
        when(medalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        loader.run(new DefaultApplicationArguments());

        ArgumentCaptor<List<Medal>> medalsCaptor = listCaptor();
        org.mockito.Mockito.verify(medalRepository).saveAll(medalsCaptor.capture());

        Medal savedPasswordMedal = medalsCaptor.getValue().stream()
            .filter(medal -> "Passordvokter".equals(medal.getName()))
            .findFirst()
            .orElseThrow();

        assertThat(savedPasswordMedal.getId()).isEqualTo(16L);
        assertThat(savedPasswordMedal.getDescription()).isEqualTo("Fullfør Passordbanken.");
        assertThat(savedPasswordMedal.getImageUrl()).isEqualTo("/medals/stop-6.png");
        assertThat(savedPasswordMedal.getStop().getOrderIndex()).isEqualTo(6);
    }

    @Test
    void run_throwsWhenExistingTasksContainDuplicateTaskSeedKey() {
        StopRepository stopRepository = mock(StopRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);
        MedalRepository medalRepository = mock(MedalRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DataLoader loader = new DataLoader(stopRepository, taskRepository, medalRepository, objectMapper);

        Stop passwordStop = new Stop();
        passwordStop.setId(6L);
        passwordStop.setName("Passordbanken");
        passwordStop.setOrderIndex(6);
        passwordStop.setTheme("PASSWORD");

        Task first = new Task();
        first.setId(201L);
        first.setStop(passwordStop);
        first.setOrderIndex(2);
        first.setTaskType(TaskType.PASSWORD);
        first.setTitle("first");
        first.setContentJson("{\"type\":\"CHOICE\"}");
        first.setCorrectAnswerJson("{\"selected\":\"a\"}");

        Task second = new Task();
        second.setId(202L);
        second.setStop(passwordStop);
        second.setOrderIndex(2);
        second.setTaskType(TaskType.PASSWORD);
        second.setTitle("second");
        second.setContentJson("{\"type\":\"CHOICE\"}");
        second.setCorrectAnswerJson("{\"selected\":\"b\"}");

        when(stopRepository.count()).thenReturn(1L);
        when(stopRepository.findAllByOrderByOrderIndexAsc()).thenReturn(List.of(passwordStop));
        when(stopRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.count()).thenReturn(2L);
        when(taskRepository.findAll()).thenReturn(List.of(first, second));
        when(medalRepository.count()).thenReturn(0L);
        when(medalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> loader.run(new DefaultApplicationArguments()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Duplicate taskSeedKey");
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

        ArgumentCaptor<List<Task>> tasksCaptor = listCaptor();
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

    private int correctOptionIndex(JsonNode question) {
        String correct = question.path("correct").asText();
        ArrayNode options = (ArrayNode) question.path("options");
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).asText().equals(correct)) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private <T> ArgumentCaptor<List<T>> listCaptor() {
        return ArgumentCaptor.forClass((Class<List<T>>) (Class<?>) List.class);
    }
}
