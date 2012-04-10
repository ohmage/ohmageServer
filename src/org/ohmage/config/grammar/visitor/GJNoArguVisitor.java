//
// Generated by JTB 1.3.2
//

package org.ohmage.config.grammar.visitor;
import org.ohmage.config.grammar.syntaxtree.Condition;
import org.ohmage.config.grammar.syntaxtree.Conjunction;
import org.ohmage.config.grammar.syntaxtree.Expression;
import org.ohmage.config.grammar.syntaxtree.Id;
import org.ohmage.config.grammar.syntaxtree.NodeList;
import org.ohmage.config.grammar.syntaxtree.NodeListOptional;
import org.ohmage.config.grammar.syntaxtree.NodeOptional;
import org.ohmage.config.grammar.syntaxtree.NodeSequence;
import org.ohmage.config.grammar.syntaxtree.NodeToken;
import org.ohmage.config.grammar.syntaxtree.Sentence;
import org.ohmage.config.grammar.syntaxtree.SentencePrime;
import org.ohmage.config.grammar.syntaxtree.Start;
import org.ohmage.config.grammar.syntaxtree.Value;

/**
 * All GJ visitors with no argument must implement this interface.
 */

public interface GJNoArguVisitor<R> {

	//
	// GJ Auto class visitors with no argument
	//

	public R visit(NodeList n);
	public R visit(NodeListOptional n);
	public R visit(NodeOptional n);
	public R visit(NodeSequence n);
	public R visit(NodeToken n);

	//
	// User-generated visitor methods below
	//

	/**
	 * f0 -> Sentence()
	 * f1 -> <EOF>
	 */
	public R visit(Start n);

	/**
	 * f0 -> Expression() SentencePrime()
	 *       | "(" Sentence() ")" SentencePrime()
	 */
	public R visit(Sentence n);

	/**
	 * f0 -> ( Conjunction() Sentence() SentencePrime() )?
	 */
	public R visit(SentencePrime n);

	/**
	 * f0 -> Id()
	 * f1 -> Condition()
	 * f2 -> Value()
	 */
	public R visit(Expression n);

	/**
	 * f0 -> <TEXT>
	 */
	public R visit(Id n);

	/**
	 * f0 -> "=="
	 *       | "!="
	 *       | "<"
	 *       | ">"
	 *       | "<="
	 *       | ">="
	 */
	public R visit(Condition n);

	/**
	 * f0 -> <TEXT>
	 */
	public R visit(Value n);

	/**
	 * f0 -> "and"
	 *       | "or"
	 */
	public R visit(Conjunction n);

}

