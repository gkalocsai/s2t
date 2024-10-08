package compilation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;



import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.SyntaxTreeBuilder;
import syntax.tree.tools.RuleInterval;

public class Transpiler {

	private String source;
	private Grammarhost grammarhost;
	private StringBuilder sb = new StringBuilder();
	private Map<RuleInterval, RuleInterval[]> deduction;
    private Map<String, String> global = new HashMap<>();
    private SyntaxTreeBuilder stb;
    private boolean subResults;

	public Transpiler(String source, String syntaxFileContent) throws GrammarException {
		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList = rr.getAllRules();
		this.grammarhost = new Grammarhost(ruleList);
		this.source = source;
		this.stb = new SyntaxTreeBuilder(grammarhost, source, false);
	}

	public Transpiler(String source, Grammarhost gh) throws GrammarException {
		this.grammarhost = gh;
		this.source = source;
	}

    public Transpiler(String source, SyntaxTreeBuilder stb, boolean subResults) {
        this(source, stb);
        this.subResults = subResults;
    }

    public Transpiler(String source, SyntaxTreeBuilder stb) {
        this.source = source;
        this.stb = stb;
    }
	public String transpile() {

		if (sb.length() != 0) {
			return sb.toString();
		}
		if (stb == null) {
			this.stb = new SyntaxTreeBuilder(grammarhost, source, false);
		}
		deduction = stb.build();
		RuleInterval root = stb.getRoot();
        if (root == null && !subResults) {
            System.out.println(stb.getState());
            return null;
        }
        int last = -1;
        if (root == null && subResults) {
            ArrayList<RuleInterval> recognized = stb.getRecognizedRules();
            for (int i = 0; i < recognized.size(); i++) {
                RuleInterval current = recognized.get(i);
                if (current.getBegin() > last + 1) {
                    sb.append("\n--UNRESOLVED:\n");
                    sb.append(source.substring(last + 1, current.getBegin()));
                }
                sb.append("\n--FOUND: " + current.toGroupnameAndInterval() + "\n");
                doTranspile(current);
                last = current.getLast();
            }
            if (last < source.length()) {
                sb.append("\n--UNRESOLVED:\n");
                sb.append(source.substring(last + 1));
            }
            return sb.toString();
        }
        doTranspile(root);
		return sb.toString();
	}

	private void doTranspile(RuleInterval e) {
		Rule r = e.getRule();
		RuleInterval[] ra = deduction.get(e);
        if (ra == null) {
            return;
        }
		CompilationElement[] compArray = r.getCompilation();

		for (int i = 0; i < ra.length; i += r.getRightSideLength()) {

			for (CompilationElement ce : compArray) {
				CompilationElementType type = ce.getType();
				if (type == CompilationElementType.ESCAPED_STRING) {
					sb.append(ce.getBase());
				} else if (type == CompilationElementType.SOURCE_REFERENCE) {
					int x = r.getIndexOfLabel(ce.getBase());
					sb.append(source.substring(ra[x + i].getBegin(), ra[x + i].getLast() + 1));
				} else if (type == CompilationElementType.GROUP_REFERENCE) {
                    int x = r.getIndexOfLabel(ce.getBase());
                    doTranspile(ra[x + i]);
                } else if (type == CompilationElementType.PUT) {
                    String ceKey = ce.getBase();
                    StringBuilder value = buildInnerSource(r, ra, i, ce);
                    global.put(ceKey, value.toString());
                } else if (type == CompilationElementType.GET) {
                    String ceKey = ce.getBase();
                    String val = global.get(ceKey);
                    if (val != null)
                        sb.append(val);
                }
                else if (type == CompilationElementType.INNER_CALL) {
                    StringBuilder innerSource = buildInnerSource(r, ra, i, ce);
                    Set<String> originalRoot = grammarhost.getRootGroups();
                    Set<String> oneRoot = new HashSet<>();
                    oneRoot.add(ce.getBase());
                    grammarhost.setRootGroups(oneRoot);
					String innnerSrc = innerSource.toString();
					Transpiler trp=new Transpiler(innnerSrc, grammarhost);
					sb.append(trp.transpile());
                    grammarhost.setRootGroups(originalRoot);
				}
			}
		}
	}

    public StringBuilder buildInnerSource(Rule r, RuleInterval[] ra, int i, CompilationElement ce) {
        StringBuilder innerSource = new StringBuilder();
        for( CompilationElement p: ce.getParams()) {
        	CompilationElementType type2=p.getType();  
        	if (type2 == CompilationElementType.ESCAPED_STRING) {
        		innerSource.append(p.getBase());
        	} else if (type2 == CompilationElementType.SOURCE_REFERENCE) {
        		int x = r.getIndexOfLabel(p.getBase());
        		innerSource.append(source.substring(ra[x + i].getBegin(), ra[x + i].getLast() + 1));
        	}else if (type2 == CompilationElementType.GROUP_REFERENCE) {
        		int x = r.getIndexOfLabel(p.getBase());
        		innerSource.append(doTranspileInner(ra[x + i]));
        	}
        }
        return innerSource;
    }

	private String doTranspileInner(RuleInterval e) {
		StringBuilder sb=new StringBuilder();
		Rule r = e.getRule();
		RuleInterval[] ra = deduction.get(e);
		CompilationElement[] compArray = r.getCompilation();

		for (int i = 0; i < ra.length; i += r.getRightSideLength()) {

			for (CompilationElement ce : compArray) {
				CompilationElementType type = ce.getType();
				if (type == CompilationElementType.ESCAPED_STRING) {
					sb.append(ce.getBase());
				} else if (type == CompilationElementType.SOURCE_REFERENCE) {
					int x = r.getIndexOfLabel(ce.getBase());
					sb.append(source.substring(ra[x + i].getBegin(), ra[x + i].getLast() + 1));
				} else if (type == CompilationElementType.GROUP_REFERENCE) {
					int x = r.getIndexOfLabel(ce.getBase());
					sb.append (doTranspileInner(ra[x + i]));
				}	
			}
		}
		return sb.toString();
	}
}
