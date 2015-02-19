package cn.edu.scut.kapok.distributed.common.node.impl.zk;

interface NodeEventListener<T> {
    void onAdd(T nodeInfo);

    void onUpdate(T nodeInfo);

    void onRemove(T nodeInfo);
}
