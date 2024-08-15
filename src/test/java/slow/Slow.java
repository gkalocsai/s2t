package slow;

import org.junit.Test;

import compilation2.Transpiler;
import hu.kg.util.StringLoadUtil;
import read.RuleReader;
import syntax.grammar.GrammarException;

public class Slow {




	@Test
	public void kivi() throws GrammarException{

		String stt=StringLoadUtil.loadResource("kivi.stt");
		RuleReader rr = new RuleReader(stt);
		stt=rr.getPreprocessed();
		String src=StringLoadUtil.loadResource("kivi.src");
	    //src=src.substring(0, 6);
		Transpiler trp=new Transpiler(src, stt);

		System.out.println(trp.transpile());


		//SyntaxTreePic pac=new SyntaxTreePic(stb.getTree());

		//System.out.println(pac.getPic());

	//	Assert.assertTrue(result);
	}

}
