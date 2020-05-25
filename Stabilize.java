public class Stabilize implements Runnable {
	private Node node;

	/**
	 * 
	 * @param node
	 */
	Stabilize(Node node) {
		this.node = node;
	}

	public void run() {
		try {
			this.node.stabilize();
			this.node.fixFingers();
			this.node.getPredecessor();

			System.out.print("Your id is " + node.getId() + '.');
				
			node.printNeighbors();
			System.out.println();
                
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}