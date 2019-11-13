package nextstep.di.factory;

import nextstep.di.factory.example.controller.QnaController;
import nextstep.di.factory.example.service.MyQnaService;
import nextstep.di.factory.example.service.TestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BeanFactoryTest {

    private BeanFactory2 beanFactory;

    @BeforeEach
    void setup() throws InvocationTargetException, IllegalAccessException {
//        Set<Class<?>> preInstantiateClazz = BeanScanner.scan("nextstep.di.factory.example");
        Set<Class<?>> preInstantiateClazz = BeanScanner.scanConfiguration("nextstep.di.factory.example.config");
        beanFactory = new BeanFactory2(preInstantiateClazz);
        beanFactory.initialize();
    }

    @Test
    void di() {
        QnaController qnaController = beanFactory.getBean(QnaController.class);

        assertNotNull(qnaController);
        assertNotNull(qnaController.getQnaService());

        MyQnaService qnaService = qnaController.getQnaService();
        assertNotNull(qnaService.getUserRepository());
        assertNotNull(qnaService.getQuestionRepository());
    }

    @Test
    @DisplayName("같은 종류 어노테이션을 가진 빈을 주입한다.")
    void injectSameAnnotationType() {
        TestService testService = beanFactory.getBean(TestService.class);

        assertNotNull(testService);
        assertNotNull(testService.getMyQnaService());
    }

    @Test
    @DisplayName("Configuration 클래스를 통해 bean을 등록한다.")
    void beanCreationByConfiguration() {
        // TODO: 19. 11. 13. datasource 이용하여 테스트 작성
    }
}
