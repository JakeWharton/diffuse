class Synthetics {
  private int thing;

  private static class Inner {
    Inner(Synthetics synthetics) {
      System.out.println(synthetics.thing);
      synthetics.thing = 1;
      synthetics.thing++;
      synthetics.thing--;
      ++synthetics.thing;
      --synthetics.thing;
    }
  }
}
