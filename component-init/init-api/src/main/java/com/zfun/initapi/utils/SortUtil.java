package com.zfun.initapi.utils;

import com.zfun.initapi.internal.IInitProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SortUtil {

    //只访问 list，不会对其进行更改；
    //只会返回有依赖关系的List；
    //排序结果为：第一个元素是依赖关系的开头（所以应该从第一个元素开始依此初始化）；
    //return 不为null；
    public static List<IInitProvider> sortDependencies(List<IInitProvider> list) {
        final Set<String> dependentNames = new HashSet<>();//确保排序结果中只包含具有依赖关系的元素
        // 创建有向图的邻接表表示
        final Map<String, List<String>> graph = new HashMap<>();
        for (IInitProvider a : list) {
            final String name = a.name();
            if (null == name || name.length()==0){
                continue;
            }
            final String[] depOnArray = a.dependsOn();
            if (depOnArray.length==0){
                continue;
            }
            List<String> value = graph.get(name);
            if (null == value) {
                value = new ArrayList<>();
                graph.put(name, value);
            }
            for (String aDep : depOnArray){
                if (null == aDep || aDep.length()==0){
                    continue;
                }
                value.add(aDep);
                dependentNames.add(aDep);
            }
        }


        // 使用拓扑排序获取有向图的排序结果
        List<String> sortedNames = topologicalSort(graph,dependentNames);

        // 根据排序结果构建排序后的A对象列表
        List<IInitProvider> sortedList = new ArrayList<>();
        for (String name : sortedNames) {
            for (IInitProvider a : list) {
                if (a.name().equals(name)) {
                    sortedList.add(a);
                    break;
                }
            }
        }

        return sortedList;
    }

    private static List<String> topologicalSort(Map<String, List<String>> graph,Set<String> dependentNames) {
        List<String> sortedNames = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> cycleDetection = new HashSet<>();

        for (String name : graph.keySet()) {
            if (!visited.contains(name) && dependentNames.contains(name)) {
                if (!topologicalSortDFS(graph, name, visited, cycleDetection, sortedNames)) {
                    throw new IllegalArgumentException("Init Dependency cycle detected!");
                }
            }
        }

        //Collections.reverse(sortedNames);
        return sortedNames;
    }

    private static boolean topologicalSortDFS(Map<String, List<String>> graph, String name, Set<String> visited,
                                              Set<String> cycleDetection, List<String> sortedNames) {
        visited.add(name);
        cycleDetection.add(name);

        List<String> dependencies = graph.get(name);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (cycleDetection.contains(dependency)) {
                    return false; // 发现依赖关系环路
                }

                if (!visited.contains(dependency)) {
                    if (!topologicalSortDFS(graph, dependency, visited, cycleDetection, sortedNames)) {
                        return false; // 依赖关系环路
                    }
                }
            }
        }

        cycleDetection.remove(name);
        sortedNames.add(name);
        return true;
    }
}
