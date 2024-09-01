package compilation;

import java.util.List;
import java.util.Map;

import read.RuleReader;
import syntax.Rule;
import syntax.grammar.GrammarException;
import syntax.grammar.Grammarhost;
import syntax.tree.builder.RuleInterval;
import syntax.tree.builder.STreeBuilder;

public class Transpiler {

	private String source;
	private Grammarhost grammarhost;
    private StringBuilder sb=new StringBuilder();
	private Map<RuleInterval, RuleInterval[]> deduction;

	public Transpiler(String source, String syntaxFileContent) throws GrammarException  {

		RuleReader rr = new RuleReader(syntaxFileContent);
		List<Rule> ruleList=rr.getAllRules();
		this.grammarhost = new Grammarhost(ruleList);
		this.source=source;

	}

	public Transpiler(String source, Grammarhost gh, String root) throws GrammarException  {
            this(source,gh);
            this.grammarhost.setRootGroup(root);
	}

	public Transpiler(String source, Grammarhost gh) throws GrammarException  {
		this.grammarhost = gh;
		this.source=source;

	}


	public String transpile() {

		if(!sb.isEmpty()) {
			return sb.toString();
		}
		STreeBuilder stb=new STreeBuilder(grammarhost, source);
		deduction = stb.build();
		RuleInterval root=stb.getRoot();
		if(root == null) {
			return null;
		}


		doTranspile(root);
		return sb.toString();
	}


	private void doTranspile(RuleInterval e) {
		Rule r=e.getRule();
		RuleInterval[] ra = deduction.get(e);
		CompilationElement[] compArray = r.getCompilation();
		for(CompilationElement ce:compArray) {
			char type=ce.getType();
			if(type =='\"') {
				sb.append(ce.getBase());
			} else if(type == ' ') {
				int x = r.getIndexOfLabel(ce.getBase());
				sb.append(source.substring(ra[x].getBegin(), ra[x].getLast()+1));
			} else if(type == '*') {
				int x = r.getIndexOfLabel(ce.getBase());
				doTranspile(ra[x]);
			}
		}
	}

}