import cn.edu.scut.kapok.distributed.worker.retriever.BBTRetriever
import cn.edu.scut.kapok.distributed.worker.spi.Retriever
import com.google.inject.AbstractModule

class WorkerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Retriever.class).to(BBTRetriever.class)
    }
}
