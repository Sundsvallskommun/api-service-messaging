package archunit.support;

import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class Conditions {

    private Conditions() { }

    public static ArchCondition<JavaClass> NOT_HAVE_PUBLIC_SETTERS = new ArchCondition<>("not have public setters") {

        @Override
        public void check(final JavaClass item, final ConditionEvents events) {
            item.getMethods().stream()
                .filter(this::isSetter)
                .forEach(method -> {
                    var message = String.format("Public setter %s found", method.getName());

                    events.add(SimpleConditionEvent.violated(item, message));
                });
        }

        private boolean isSetter(final JavaMethod method) {
            return method.getName().startsWith("set") && method.getModifiers().contains(PUBLIC);
        }
    };
}
