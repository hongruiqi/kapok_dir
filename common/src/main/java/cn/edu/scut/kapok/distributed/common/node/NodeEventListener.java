package cn.edu.scut.kapok.distributed.common.node;

public interface NodeEventListener<T> {
    void onAdd(T nodeInfo);

    void onUpdate(T nodeInfo);

    void onRemove(T nodeInfo);
}
