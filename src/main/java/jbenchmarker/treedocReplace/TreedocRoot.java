/**
 * Replication Benchmarker
 * https://github.com/score-team/replication-benchmarker/
 * Copyright (C) 2013 LORIA / Inria / SCORE Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jbenchmarker.treedocReplace;

import jbenchmarker.treedoc.*;
import java.util.List;
import jbenchmarker.treedoc.TreedocIdentifier.Recorder;

/**
 * Root of Treedoc tree, public API to tree-based implementation.
 * 
 * @author mzawirski
 */
public class TreedocRoot<T> extends TreedocNode<T> {
	private final UniqueTagGenerator tagGenerator;

	public TreedocRoot(final UniqueTagGenerator tagGenerator) {
		super(null);
		this.tagGenerator = tagGenerator;
	}

	public void insertAt(final TreedocIdentifier id, final List<T> content) {
		findAndInsertNode(id.iterator(), content);
	}

	public void deleteAt(final TreedocIdentifier id) {
		findAndDeleteNode(id.iterator());
	}

	public TreedocIdentifier insertAt(int index, final List<T> content,
			final int replicaId, boolean type) { //type = true if replace
		final TreedocNode newNode = new TreedocNode(
				tagGenerator.nextTag(replicaId));
		newNode.createBalancedSubtreeOfContent(content, 0, content.size());
		final Recorder idRecorder = new Recorder();
		final TreedocNode precedingNode;
                if (index == 0) {
			precedingNode = this;
			precedingNode.subtreeSize += content.size();
		} else {
			precedingNode = findNthContentAndAlterSize(new DecreasingCounter(
					index), idRecorder, content.size());
		}
                if(!type)
                    precedingNode.insertAfter(newNode, idRecorder);
                else
                    precedingNode.insertBefore(newNode, idRecorder);
                
		return idRecorder.createIdentifier();
	}

	public TreedocIdentifier deleteAt(int index) {
		final Recorder idRecorder = new Recorder();
		final TreedocNode node = findNthContentAndAlterSize(
				new DecreasingCounter(index + 1), idRecorder, -1);
		node.tombstone = true;
		return idRecorder.createIdentifier();
	}

	public int getContentSize() {
		return getSubtreeSize();
	}

	public String getContent() {
		final StringBuilder buffer = new StringBuilder(getSubtreeSize());
		getSubtreeContent(buffer);
		if (content instanceof Character && buffer.length() != getSubtreeSize())
			throw new RuntimeException("Unexpected document size: got "
					+ buffer.length() + ", want " + getSubtreeSize());
		return buffer.toString();
	}

	public void printStats() {
		//System.out.println("Nodes in the tree: " + getNodesNumber());
		//System.out.println("Non-empty nodes in the tree: " + getContentSize());
	}

	@Override
	protected boolean isRoot() {
		return true;
	}
}
