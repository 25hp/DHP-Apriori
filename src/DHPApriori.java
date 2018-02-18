import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Apriori with Direct Hashing and Pruning.
 */
public class DHPApriori {
	List<Map<List<String>, Integer>> F = new ArrayList<>();
	List<Map<List<String>, Integer>> H = new ArrayList<>();
	Map<String, Integer> itemCount = new HashMap<>();
	List<List<String>> database;
	List<Map<String, Integer>> db = new ArrayList<>();
	int minSupCount;
	void fillDatabase(List<String> transaction) {
        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (String item : transaction) {
            if (hm.get(item) == null) {
                hm.put(item, 1);
            } else {
                hm.put(item, hm.get(item) + 1);
            }
        }
        db.add(hm);
    }
	
	private int getFrequency(List<String> itemset) {
        int frequency = 0;
        for (Map<String, Integer> transaction : db) {
            int minimum = 10000;
            for (String item : itemset) {
                if (transaction.get(item) != null) {
                   if (transaction.get(item) < minimum) {
                       minimum = transaction.get(item);
                   }
                } else {
                    minimum = 0;
                }
            }
            frequency += minimum;
        }
        return frequency;
    }

	public List<Pattern> getAllFrequentItemsets() {
		List<Pattern> all = new ArrayList<>();
		for (Map<List<String>, Integer> Fk : F) {
			for (List<String> itemset : Fk.keySet()) {
				all.add(new Pattern(itemset, Fk.get(itemset)));
			}
		}
		all.sort(new Comparator<Pattern>() {
			@Override
			public int compare(Pattern lhs, Pattern rhs) {
				return lhs.supportValue > rhs.supportValue ? -1 : (lhs.supportValue < rhs.supportValue) ? 1 : 0;
			}
		});
		return all;
	}

	public DHPApriori(List<List<String>> database, int minSupCount) {
		this.minSupCount = minSupCount;
		this.database = database;
		for (List<String> transaction : database) {
			fillDatabase(transaction);
		}

		countSupportAndGenerateH2();
//		System.out.println(itemCount);
//		System.out.println(H.get(2));
		generateF1();
//		System.out.println(F.get(1));

		for (int k = 2; F.get(k - 1).size() != 0; k++) {
//			System.out.println("k = " + k);
			generateFAndNextH(k);
//			System.err.println("F" + k + ": " + F);
//			System.err.println("H" + (k + 1) + ": " + H);
		}
	}

	private void countSupportAndGenerateH2() {
		F.add(new HashMap<>());
		H.add(new HashMap<>());
		H.add(new HashMap<>()); // Make F and H 1-indexed
//		System.out.println("Counting support...");
		Map<List<String>, Integer> H2 = new HashMap<>();
		for (List<String> transaction : database) {
			for (String item : transaction) {
				if (itemCount.get(item) != null) {
					itemCount.put(item, itemCount.get(item) + 1);
				} else {
					itemCount.put(item, 1);
				}
			}
			
			for (int i = 0; i < transaction.size(); i++) {
				for (int j = i + 1; j < transaction.size(); j++) {
					List<String> itemset = new ArrayList<>();
					itemset.add(transaction.get(i));
					itemset.add(transaction.get(j));
					if (H2.get(itemset) != null) {
						H2.put(itemset, H2.get(itemset) + 1);
					} else {
						H2.put(itemset, 1);
					}
				}
			}
		}
		// Prune H2
		H2.entrySet().removeIf(e -> (e.getValue() < minSupCount));
		H.add(H2);
	}

	private void generateF1() {
		Map<List<String>, Integer> F1 = new HashMap<>();
		for (String item : itemCount.keySet()) {
			if (itemCount.get(item) >= minSupCount) {
				List<String> itemset = new ArrayList<>();
				itemset.add(item);
				F1.put(itemset, itemCount.get(item));
			}
		}
		F.add(F1);
	}

	private void generateFAndNextH(int k) {
		Map<List<String>, Integer> Ck = generateCk(k);
		Map<List<String>, Integer> nextH = generateNextH(k);
		nextH.entrySet().removeIf(e -> (e.getValue() < minSupCount));
		H.add(nextH);
		F.add(generateFk(Ck));
	}

	private Map<List<String>, Integer> generateCk(int k) {
		Map<List<String>, Integer> Ck = new HashMap<>();
		for (List<String> x : join(k - 1)) {
//			System.out.println("Join: " + x);
			if (H.get(k).containsKey(x) && H.get(k).get(x) >= minSupCount) {
				Ck.put(x, getFrequency(x));
			}
		}
//		for (List<String> transaction : database) {
//			System.out.println("Transaction: " + transaction);
//			for (List<String> itemset : getAllKCombinations(transaction, k)) {
//				System.out.println("combi: " + itemset);
//				if (Ck.containsKey(itemset)) {
//					System.out.println("Yes");
//					Ck.put(itemset, Ck.get(itemset) + 1);
//				}
//			}
//		}
//		System.out.println("C" + k + ": " + Ck);
		return Ck;
	}

	private Map<List<String>, Integer> generateNextH(int k) {
		Map<List<String>, Integer> nextH = new HashMap<>();
		for (List<String> transaction : database) {
			for (List<String> itemset : getAllKCombinations(transaction, k + 1)) {
				boolean badExists = false;
				for (int i = 0; i < itemset.size(); i++) {
					List<String> z = new ArrayList<>(); // k - subset
					for (int j = 0; j < itemset.size(); j++) {
						if (j != i) {
							z.add(itemset.get(j));
						}
					}
					if (!H.get(k).containsKey(z) || H.get(k).get(z) < minSupCount) {
						badExists = true;
						break;
					}
				}
				if (badExists == false) {
					if (nextH.containsKey(itemset)) {
						nextH.put(itemset, nextH.get(itemset) + 1);
					} else {
						nextH.put(itemset, 1); // is it 0
					}
				}
			}
		}
		return nextH;
	}

	private Map<List<String>, Integer> generateFk(Map<List<String>, Integer> Ck) {
		Map<List<String>, Integer> Fk = new HashMap<>();
		for (List<String> itemset : Ck.keySet()) {
			if (Ck.get(itemset) >= minSupCount) {
				Fk.put(itemset, Ck.get(itemset));
			}
		}
		return Fk;
	}

	private List<List<String>> getAllKCombinations(List<String> list, int k) {
		Combination c = new Combination();
		return c.getAllCombinations(list, k);
	}

	private List<List<String>> toList(Map<List<String>, Integer> m) {
		List<List<String>> l = new ArrayList<>();
		for (List<String> list : m.keySet()) {
			l.add(list);
		}
		return l;
	}

	private List<List<String>> join(int k) {
		List<List<String>> Fk = toList(F.get(k));
		List<List<String>> joined = new ArrayList<>();
		for (int i = 0; i < Fk.size(); i++) {
			for (int j = i + 1; j < Fk.size(); j++) {
				List<String> a = Fk.get(i);
				List<String> b = Fk.get(j);
				if (isJoinable(a, b)) {
					List<String> merged = new ArrayList<String>();
					for (int l = 0; l < a.size() - 1; l++) {
						merged.add(a.get(l));
					}
					String s1 = a.get(a.size() - 1);
					String s2 = b.get(b.size() - 1);
					if (s1.compareTo(s2) < 0) {
						merged.add(s1);
						merged.add(s2);
					} else {
						merged.add(s2);
						merged.add(s1);
					}
					joined.add(merged);
				}
			}
		}
		return joined;
	}

	private boolean isJoinable(List<String> a, List<String> b) {
		for (int i = 0; i < (a.size() - 1); i++) {
			if (!a.get(i).equals(b.get(i))) {
				return false;
			}
		}
		return true;
	}

}

class Combination {
	private List<List<String>> allCombinations = new ArrayList<>();

	public List<List<String>> getAllCombinations(List<String> l, int k) {
		genCombinations((String[]) l.toArray(), k, 0, new String[k]);
		return allCombinations;
	}

	private void genCombinations(String[] arr, int len, int startPosition, String[] result) {
		if (len == 0) {
			List<String> t = new ArrayList<>();
			for (int i = 0; i < result.length; i++) {
				t.add(result[i]);
			}
			allCombinations.add(t);
			return;
		}
		for (int i = startPosition; i <= arr.length - len; i++) {
			result[result.length - len] = arr[i];
			genCombinations(arr, len - 1, i + 1, result);
		}
	}
	
}

class Pattern {
	List<String> itemsList;
	int supportValue;

	public Pattern(List<String> items, int support) {
		this.itemsList = items;
		this.supportValue = support;
	}

	@Override
	public String toString() {
		return itemsList.toString() + "->" + supportValue;
	}
}