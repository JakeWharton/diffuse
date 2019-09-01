public final class Desugaring implements Runnable {
  private final Runnable delegate;

  private Desugaring(Runnable delegate) {
    this.delegate = delegate;
  }

  @Override public void run() {
    delegate.run();
  }

  public static void main(String[] args) {
    new Desugaring(() -> {
      System.out.println("Hello, world!");
    }).run();
  }
}
