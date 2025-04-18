// stores each block and its path information
public class Node {
    // position of the block in the minecraft world
    public BlockPos pos;
    // parent of the node
    public Node parent;
    // cost from start
    public double g; 
    // heuristic cost to goal
    public double h; 

    // A node is composed of a position, a parent, 
    // a cost to start from, and a cost to reach.
    public Node(BlockPos pos, Node parent, double g, double h) {
        this.pos = pos;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }

    // Gets the manhatten distance between two blocks
    private int getManhattenDistance(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) 
         + Math.abs(a.getY() - b.getY()) 
         + Math.abs(a.getZ() - b.getZ());
    }

    // F is the cost combined for starting from and getting to the nodes
    public double getF() {
        return g + h;
    }

    // Calculates the movement cost of moving in a direction
    private static double getMovementCost(World world, BlockPos from, BlockPos to) {
    BlockState blockBelow = world.getBlockState(to.down());
    BlockState blockAt = world.getBlockState(to);

    // Check the blocks the player is currently occupying in the world
    if (blockAt.isAir() || blockAt.getMaterial().isReplaceable()) {
        // Check the block below the player
        // We do not want to go near lava
        if (blockBelow.isOf(Blocks.LAVA)) return Double.POSITIVE_INFINITY;
        // Water is discouraged as it will slow us down
        if (blockBelow.isOf(Blocks.WATER)) return 5;
        // Ladders are fine but other paths are better
        if (blockBelow.isOf(Blocks.LADDER)) return 3;
        // avoid soul sand
        if (blockBelow.isOf(Blocks.SOUL_SAND)) return 4;
        // avoid honey
        if (blockBelow.isOf(Blocks.HONEY_BLOCK)) return 6;
        // ice is ok but worse than regular movemtn
        if (blockBelow.isOf(Blocks.ICE)) return 2;

        // Normal movement
        return 1; 
        }
    return Double.POSITIVE_INFINITY;
    }

    // gets a block's neighboring positions
    private static List<BlockPos> getNeighbors(BlockPos pos) {
    return List.of(
        pos.north(),
        pos.east(),
        pos.south(),
        pos.west(),
        // jumps
        pos.up(),
        // drops
        pos.down()
        );
    }
    
    // Finds the shortest possible path using A* from one point to another
    public static List<BlockPos> findPath(World world, BlockPos start, BlockPos goal, int maxNodes) {
    // A* uses priority queue with the lowest f value node being the highest priority
    // frontier
    PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
    // Hash map to store all nodes
    Map<BlockPos, Node> allNodes = new HashMap<>();
    // traversed nodes
    Set<BlockPos> closedSet = new HashSet<>();

    // The starting node from which you start autopathing has no parents and has a g of 0
    Node startNode = new Node(start, null, 0, getManhattenDistance(start, goal));
    openSet.add(startNode);
    allNodes.put(start, startNode);

    // while there is nothing traversed yet, if the current position is the goal, then that is the
    // shortest path, otherwise add the current position to the closed set and continue with the
    // neighboring blocks
    while (!openSet.isEmpty() && allNodes.size() < maxNodes) {
        Node current = openSet.poll();

        if (current.pos.equals(goal)) {
            return reconstructPath(current);
        }

        closedSet.add(current.pos);

        for (BlockPos neighborPos : getNeighbors(current.pos)) {
            // if neighboring blocks are already in the closed set, skip
            if (closedSet.contains(neighborPos)) continue;

            // set a temporary value for g based on the movement cost and the current g value
            double movementCost = getMovementCost(world, current.pos, neighborPos);

            // Skip if unwalkable
            if (movementCost == Double.POSITIVE_INFINITY) continue;

            // Vertical penalty: discourage falls greater than 3 blocks
            int verticalDrop = current.pos.getY() - neighborPos.getY();
            if (verticalDrop > 3) {
                // Apply extra cost for big falls (e.g., 3 per extra block)
                movementCost += (verticalDrop - 3) * 3;
            }
            
            // set a temporary g value by adding the current g value and the movement cost
            double tempG = current.g + movementCost;

            // if G is infinity, skip
            if (tempG == Double.POSITIVE_INFINITY) continue;

            // the neighboring node
            Node neighborNode = allNodes.getOrDefault(neighborPos, new Node(neighborPos, null, Double.POSITIVE_INFINITY, getManhattenDistance(neighborPos, goal)));

            // if the G value is less than that of a neighboring node's g value, replace the neighboring
            // node's g value with temp g and make the neighbor node's parent the current node
            if (tempG < neighborNode.g) {
                neighborNode.g = tempG;
                neighborNode.parent = current;

                // if the frontier does not have the neighbor node in it, add it
                if (!openSet.contains(neighborNode)) {
                    openSet.add(neighborNode);
                }
                // mark the node
                allNodes.put(neighborPos, neighborNode);
            }
        }
    }

    // No path found
    return Collections.emptyList(); 
}

// reconstruct the shortest path from one block to another
private static List<BlockPos> reconstructPath(Node endNode) {
    List<BlockPos> path = new ArrayList<>();
    Node current = endNode;
    while (current != null) {
        path.add(current.pos);
        current = current.parent;
    }
    Collections.reverse(path);
    return path;
    }

}