package edu.school21.app;


import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.Set;

public class Program {
    private static Scanner scanner;

    public static void main(String[] args) throws NoSuchFieldException {
        scanner = new Scanner(System.in);
        Set<Class<?>> userClasses = getUserClasses();

        System.out.println("Classes:");
        printClasses(userClasses);

        printer();
        String className = getInput("Enter class name:");
        if (hasClass(userClasses, className)) {
            printHeader("fields:");
            printFields(className);

            printHeader("methods:");
            printMethods(className);

            printHeader("Let’s create an object.");
            Object obj = createObjectWithUserInput(className);
            System.out.println("Object created: " + obj);
            printer();

            String fieldName = getInput("Enter name of the field for changing:");
            String newFieldName = getInput("Enter String value:");
            changeFieldName(obj, fieldName, newFieldName);
            System.out.println("Object updated: " + obj);
            printer();

            String methodName = getInput("Enter name of the method for call:");
            int methodArg = -1;
            String[] methodParts = methodName.split("\\(");
            if (methodParts.length > 1) {
                String argPrompt = methodParts[1].replaceAll("\\)", "") + ":";
                methodArg = Integer.parseInt(getInput(argPrompt));
            }
                Object result = callMethod(obj, methodParts[0], methodArg);

            if (result != null) {
                printHeader("Method returned:");
                System.out.println(result.toString());
            }
        } else {
            System.out.println("Class not found!");
        }
    }

    private static Set<Class<?>> getUserClasses() {
        Reflections reflections = new Reflections("edu.school21.models", new SubTypesScanner(false));
        return reflections.getSubTypesOf(Object.class);
    }

    private static void printer() {
        System.out.println("---------------------");
    }
    private static void printHeader(String message) {
        System.out.println("---------------------");
        System.out.println(message);
    }

    private static void printClasses(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            System.out.println("  - " + clazz.getSimpleName());
        }
    }

    private static boolean hasClass(Set<Class<?>> classes, String className) {
        for (Class<?> clazz : classes) {
            if (clazz.getSimpleName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    private static void printFields(String className) {
        Field[] fields = getClassFields(className);
        if (fields != null) {
            for (Field field : fields) {
                System.out.println("\t" + field.getType().getSimpleName() + " " + field.getName());
            }
        }
    }

    private static Field[] getClassFields(String className) {
        try {
            Class<?> clazz = Class.forName("edu.school21.models." + className);
            return clazz.getDeclaredFields();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void printMethods(String className) {
        Method[] methods = getClassMethods(className);
        if (methods != null) {
            for (Method method : methods) {
                System.out.println("\t" + method.getName());
            }
        }
    }

    private static Method[] getClassMethods(String className) {
        try {
            Class<?> clazz = Class.forName("edu.school21.models." + className);
            return clazz.getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object createObjectWithUserInput(String className) {
        try {
            Class<?> clazz = Class.forName("edu.school21.models." + className);
            Object obj = clazz.getDeclaredConstructor().newInstance();

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String value = getInput("Enter value for field " + field.getName() + ":");
                setFieldValue(obj, field.getName(), value);
            }

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setFieldValue(Object obj, String fieldName, String value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(obj, value);
            } else if (field.getType().equals(int.class)) {
                field.set(obj, Integer.parseInt(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object callMethod(Object obj, String methodName, int arg) {
        try {
            Method[] methods = obj.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    if (method.getParameterCount() > 0) {
                        if (arg == -1) {
                            System.out.println("Method " + methodName + " requires " + method.getParameterCount() + " arguments.");
                            return null;
                        }
                        return method.invoke(obj, arg);
                    } else {
                        method.invoke(obj);
                        return null;
                    }
                }
            }
            System.out.println("Method not found.");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getInput(String prompt) {
        System.out.println(prompt);
        return scanner.next();
    }

    private static void changeFieldName(Object obj, String fieldName, String value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                field.set(obj, value);
            } else if (field.getType().equals(int.class)) {
                field.set(obj, Integer.parseInt(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
