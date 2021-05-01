package explicit;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import prism.Pair;
import prism.PrismComponent;

/**
 * Solve parity games using the priority promotion algorithm.
 */
public class PriorityPromotionSolver extends PGSolver
{

	/**
	 * Create a new parity game solver.
	 */
	public PriorityPromotionSolver(PrismComponent parent)
	{
		super(parent);
	}

	@Override
	public TGSolution solve(PG pg)
	{
		PG parityGame = pg;
		TGSolution soln = new TGSolution();

		while (!parityGame.getTG().getActiveStates().isEmpty()) {
			// (For benchmarking)
			if (Thread.currentThread().isInterrupted()) {
				return null;
			}

			Pair<Integer, RegionStrategy> dominion = searchDominion(parityGame);
			soln.get(dominion.getKey()).getRegion().or(dominion.getValue().getRegion());
			soln.get(dominion.getKey()).getStrategy().putAll(dominion.getValue().getStrategy());
			parityGame = parityGame.difference(dominion.getValue().getRegion());
		}

		return soln;
	}

	private Pair<Integer, RegionStrategy> searchDominion(PG pg)
	{
		Map<Integer, Integer> r = new HashMap<>();
		pg.getTG().getActiveStates().stream().forEach(s -> {
			r.put(s, -1); // -1 is our bottom
		});
		int p = pg.maxPriority();

		while (true) {
			// (For benchmarking)
			if (Thread.currentThread().isInterrupted()) {
				return null;
			}
			
			int alpha = p % 2 == 0 ? 1 : 2;
            int alphaBar = alpha == 1 ? 2 : 1;

			int finalP = p;

			BitSet Subgame = (BitSet) pg.getTG().getActiveStates().clone();
			r.entrySet().stream()
			.filter(entry -> entry.getValue() > finalP)
			.map(Map.Entry::getKey)
			.forEach(s -> Subgame.set(s, false));

			BitSet A = (BitSet) pg.getPriorityMap().getOrDefault(p, new BitSet()).clone();
			A.and(Subgame);
			r.entrySet().stream()
			.filter(entry -> entry.getValue() == finalP)
			.map(Map.Entry::getKey)
			.forEach(s -> A.set(s));

			RegionStrategy Z = pg.getTG().subgame(Subgame).attractor(alpha, A, parent);

			BitSet Open = Z.getRegion().stream()
					.filter(s -> pg.getTG().getPlayer(s) == alpha && 
								 !pg.getTG().someSuccessorsInSet(s, Z.getRegion()))
					.collect(BitSet::new, BitSet::set, BitSet::or);

			BitSet Esc = Z.getRegion().stream()
					.filter(s -> pg.getTG().getPlayer(s) == alphaBar)
					.flatMap(s -> pg.getTG().getSuccessors(s).stream())
					.filter(s -> !Z.getRegion().get(s))
					.collect(BitSet::new, BitSet::set, BitSet::or);

			BitSet EscAndSubgame = (BitSet) Esc.clone();
			EscAndSubgame.and(Subgame);

			if (!Open.isEmpty() || !EscAndSubgame.isEmpty()) {
				int finalP1 = p;
				Z.getRegion().stream().forEach(s -> r.put(s, finalP1));

				p = Subgame.stream()
						.filter(s -> !Z.getRegion().get(s))
						.map(s -> pg.getPriorities().get(s))
						.max().getAsInt();
			} else if (!Esc.isEmpty()) {
				p = r.entrySet().stream()
						.filter(entry -> Esc.get(entry.getKey()))
						.map(Map.Entry::getValue)
						.min(Integer::compare).get();
				int finalP1 = p;

				Z.getRegion().stream().forEach(s -> r.put(s, finalP1));
				r.entrySet().stream()
				.filter(entry -> entry.getValue() < finalP1)
				.map(Map.Entry::getKey)
				.forEach(s -> r.put(s, -1));
			} else {
				RegionStrategy Z1 = pg.getTG().attractor(alpha, Z.getRegion(), parent);
				Z1.getStrategy().putAll(Z.getStrategy());
				return new Pair<>(alpha, Z1);
			}
		}
	}

}
