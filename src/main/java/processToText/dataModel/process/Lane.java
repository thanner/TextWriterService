package processToText.dataModel.process;


public class Lane {

    private int id;
    private String name;
    private processToText.dataModel.process.Pool pool;

    public Lane(int id, String name, processToText.dataModel.process.Pool pool) {
        this.id = id;
        this.name = name;
        this.pool = pool;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public processToText.dataModel.process.Pool getPool() {
        return pool;
    }


}
