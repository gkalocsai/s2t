package compilation;

import java.util.List;
import java.util.Map;

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
	private SyntaxTreeBuilder stb;

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
		if (root == null) {
			System.out.println(stb.getLastDeduction());
			return null;
		}
		doTranspile(root);
		return sb.toString();
	}

	private void doTranspile(RuleInterval e) {
		Rule r = e.getRule();
		RuleInterval[] ra = deduction.get(e);
		CompilationElement[] compArray = r.getCompilation();

		for (int i = 0; i < ra.length; i += r.getRightSideLength()) {

			for (CompilationElement ce : compArray) {
				char type = ce.getType();
				if (type == '\"') {
					sb.append(ce.getBase());
				} else if (type == ' ') {
					int x = r.getIndexOfLabel(ce.getBase());
					sb.append(source.substring(ra[x + i].getBegin(), ra[x + i].getLast() + 1));
				} else if (type == '*') {
					int x = r.getIndexOfLabel(ce.getBase());
					doTranspile(ra[x + i]);
				}else if(type =='('){
					StringBuilder innerSource=new StringBuilder();                	
					for( CompilationElement p: ce.getParams()) {
						char type2=p.getType();  
						if (type2 == '\"') {
							innerSource.append(p.getBase());
						} else if (type2 == ' ') {
							int x = r.getIndexOfLabel(p.getBase());
							innerSource.append(source.substring(ra[x + i].getBegin(), ra[x + i].getLast() + 1));
						}else if (type2 == '*') {
							int x = r.getIndexOfLabel(p.getBase());
							innerSource.append(doTranspileInner(ra[x + i]));
						}
					}
					String originalRoot = grammarhost.getRootGroup();
				//	grammarhost.setRoot(ce.getBase());
					String innnerSrc = innerSource.toString();
					Transpiler trp=new Transpiler(innnerSrc, grammarhost);
					sb.append(trp.transpile());
					grammarhost.setRoot(originalRoot);
				}
			}
		}
	}

	private String doTranspileInner(RuleInterval e) {
		StringBuilder sb=new StringBuilder();
		Rule r = e.getRule();
		RuleInterval[] ra = deduction.get(e);
		CompilationElement[] compArray = r.getCompilation();

		for (int i = 0; i < ra.length; i += r.getRightSideLength()) {

			for (CompilationElement ce : compArray) {
				char type = ce.getType();
				if (type == '\"') {
					sb.append(ce.getBase());
				} else if (type == ' ') {
					int x = r.getIndexOfLabel(ce.getBase());
					sb.append(source.substring(ra[x + i].getBegin(), ra[x + i].getLast() + 1));
				} else if (type == '*') {
					int x = r.getIndexOfLabel(ce.getBase());
					sb.append (doTranspileInner(ra[x + i]));
				}	
			}
		}
		return sb.toString();
	}



	/*	}else if(type =='('){
				String source=buildInnerSource(ce.getParams(),r ,parentsNext);
				CompilationTree inner=new CompilationTree(source,originalSpacings,rootgroup,grammar);
				Node inn=inner.build();
				String result = inner.buildResult(inn);
				children.add(new Node(result));
			}
			else throw new RuntimeException("Internal error: Invalid compilation type.");
		}
	}
	private String buildInnerSource(CompilationElement[] params, Rule r, int reSpacingment) {
		StringBuilder sb=new StringBuilder();	
		if(params == null || params.length ==0) {
			throw new RuntimeException("Internal error: No source parameter in rule: "+r);
		}
		for(CompilationElement p:params){
			if(p.getType() == '\"') {
				sb.append(p.getBase());
			}else if(p.getType() == ' '){
				int rsIndex=r.getIndexOfLabel(p.getBase());
				if(rsIndex<0) continue;
				sb.append(createSourceSubStr(rsIndex, reSpacingment));
			}else{
				throw new RuntimeException("Bad source parameter in rule: "+r);
			}
		}
		return sb.toString();
	}
	 */
}
